package pg.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.importing.ImportingRecordsProvider;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class ImportingTasklet implements Tasklet {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;
    private final RecordsRepository recordsRepository;

    @Override
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) throws Exception {
        log.info("ImportingTasklet started");
        var importContext = JobUtil.getImportContext(contribution.getStepExecution());
        var importId = importContext.getImportId();

        importRepository.getImportingImport(importId.id());
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        var recordsPartitions = recordsRepository.findAllByParentImportId(importId);
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
            case LIBRARY_JSON_DATABASE -> null;
            case MONGO_REPOSITORY -> null;
        };
    }
}
