package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.importing.readers.LibraryJsonImportingRecordsProvider;
import pg.imports.plugin.infrastructure.spring.batch.importing.readers.MongoImportingRecordsProvider;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public abstract class ImportingTasklet implements Tasklet {
    protected final ImportRepository importRepository;
    protected final PluginCache pluginCache;
    protected final RecordsRepository recordsRepository;
    private final LibraryJsonImportingRecordsProvider dbJsonRecordsProvider;
    private final MongoImportingRecordsProvider mongoRecordsProvider;

    protected RepeatStatus execute(final List<ImportRecordsEntity> recordsPartitions, final ImportPlugin plugin, final StepContribution contribution) {
        var storingStrategy = recordsPartitions.stream().map(ImportRecordsEntity::getStrategy).findFirst().orElseThrow();

        var importRecordsProvider = resolveProvider(storingStrategy, plugin, recordsPartitions);
        var recordImporter = plugin.getImportingComponentsProvider().getRecordImporter();

        updateImportingStarted(recordsPartitions);
        var importingResult = recordImporter.importRecords(importRecordsProvider);

        if (importingResult.importingErrorCode().isPresent()) {
            JobUtil.putRejectReason(contribution.getStepExecution(), importingResult.importingErrorCode().get().toString());
            log.info("ImportingTasklet finished with error: {}", importingResult.importingErrorCode().get());
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

    private void updateImportingFinished(final List<ImportRecordsEntity> recordsPartitions) {
        var timestamp = LocalDateTime.now();
        recordsPartitions.forEach(importRecordsEntity -> importRecordsEntity.setFinishedImportingOn(timestamp));
        recordsRepository.saveAll(recordsPartitions);
    }

    @SuppressWarnings("unchecked")
    private ImportingRecordsProvider resolveProvider(final RecordsStoringStrategy strategy, final ImportPlugin plugin, final List<ImportRecordsEntity> recordsPartitions) {
        var recordsToImport = recordsPartitions.stream().map(ImportRecordsEntity::getRecordIds).flatMap(Collection::stream).toList();
        return switch (strategy) {
            case PLUGIN_DATABASE -> plugin.getImportingComponentsProvider().getImportingRecordsProvider(recordsToImport);
            case LIBRARY_JSON_DATABASE -> dbJsonRecordsProvider;
            case MONGO_REPOSITORY -> mongoRecordsProvider;
        };
    }
}
