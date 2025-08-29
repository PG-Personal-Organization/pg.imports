package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.persistence.records.RecordsStatus;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.importing.records.provider.ImportingRecordsProviderFactory;
import pg.imports.plugin.infrastructure.spring.batch.importing.tasklets.writer.ImportingErrorsWriterManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public abstract class ImportingTasklet implements Tasklet {
    protected final ImportRepository importRepository;
    protected final PluginCache pluginCache;
    protected final RecordsRepository recordsRepository;
    private final ImportingRecordsProviderFactory importingRecordsProviderFactory;
    private final ImportingErrorsWriterManager importingErrorsWriterManager;

    protected RepeatStatus execute(final List<ImportRecordsEntity> recordsPartitions, final ImportPlugin plugin, final StepContribution contribution) {
        var storingStrategy = recordsPartitions.stream().map(ImportRecordsEntity::getStrategy).findFirst().orElseThrow();

        var importRecordsProvider = importingRecordsProviderFactory.resolveProvider(storingStrategy, plugin, recordsPartitions);
        var recordImporter = plugin.getImportingComponentsProvider().getRecordImporter();

        updateImportingStarted(recordsPartitions);
        var importingResult = recordImporter.importRecords(importRecordsProvider);

        if (importingResult.getImportingErrorCode().isPresent()) {
            JobUtil.putRejectReason(contribution.getStepExecution(), importingResult.getImportingErrorCode().get());
            log.info("ImportingTasklet finished with error: {}", importingResult.getImportingErrorCode().get());

            recordsPartitions.forEach(records -> records.setRecordsStatus(RecordsStatus.FAILED));
            importingResult.getErrorMessages().ifPresentOrElse(
                    errorMessages -> {
                        importingErrorsWriterManager.writeErrors(storingStrategy, errorMessages, plugin);
                        recordsPartitions.forEach(records -> applyErrors(records, errorMessages::get));
                    },
                    () -> {
                        var defaultErrorCode = importingResult.getImportingErrorCode().get();
                        recordsPartitions.forEach(records -> applyErrors(records, id -> defaultErrorCode));
                    }
            );

            recordsRepository.saveAll(recordsPartitions);
            return RepeatStatus.FINISHED;
        }

        updateImportingFinished(recordsPartitions);
        log.info("ImportingTasklet finished");
        return RepeatStatus.FINISHED;
    }

    private void updateImportingStarted(final List<ImportRecordsEntity> recordsPartitions) {
        var timestamp = LocalDateTime.now();
        recordsPartitions.forEach(importRecordsEntity -> importRecordsEntity.setStartedImportingOn(timestamp));
        recordsRepository.saveAll(recordsPartitions);
    }

    private void applyErrors(final ImportRecordsEntity recordsEntity, final UnaryOperator<String> errorMessageProvider) {
        var recordIds = recordsEntity.getRecordIds();
        recordsEntity.setRecordIds(Collections.emptySet());
        recordsEntity.getErrorRecordIds().addAll(recordIds);
        recordsEntity.setErrorCount(recordsEntity.getErrorRecordIds().size());
        recordsEntity.getErrorMessages().putAll(recordIds.stream().collect(Collectors.toMap(Function.identity(), errorMessageProvider)));
    }

    private void updateImportingFinished(final List<ImportRecordsEntity> recordsPartitions) {
        var timestamp = LocalDateTime.now();
        recordsPartitions.forEach(importRecordsEntity -> {
            importRecordsEntity.setFinishedImportingOn(timestamp);
            importRecordsEntity.setRecordsStatus(RecordsStatus.IMPORTED);
        });
        recordsRepository.saveAll(recordsPartitions);
    }
}
