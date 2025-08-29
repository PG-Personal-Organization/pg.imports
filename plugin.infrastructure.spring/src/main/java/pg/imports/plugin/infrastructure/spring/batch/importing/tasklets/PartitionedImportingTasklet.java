package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.importing.partition.ImportingPartitioner;
import pg.imports.plugin.infrastructure.spring.batch.importing.records.provider.ImportingRecordsProviderFactory;
import pg.imports.plugin.infrastructure.spring.batch.importing.tasklets.writer.ImportingErrorsWriterManager;

import java.util.Collections;

@Log4j2
public class PartitionedImportingTasklet extends ImportingTasklet {

    public PartitionedImportingTasklet(final ImportRepository importRepository,
                                       final PluginCache pluginCache,
                                       final RecordsRepository recordsRepository,
                                       final ImportingRecordsProviderFactory importingRecordsProviderFactory,
                                       final ImportingErrorsWriterManager importingErrorsWriterManager) {
        super(importRepository, pluginCache, recordsRepository, importingRecordsProviderFactory, importingErrorsWriterManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) {
        log.info("PartitionedImportingTasklet started");
        var importContext = JobUtil.getImportContext(contribution.getStepExecution());
        var importId = importContext.getImportId();

        importRepository.getImportingImport(importId.id());
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());
        var partitionIdParam = (String) chunkContext.getStepContext().getStepExecutionContext().get(ImportingPartitioner.PARTITION_KEY);
        var partitionIds = Collections.singleton(partitionIdParam);

        var recordsPartitions = recordsRepository.findAllById(partitionIds);
        return execute(recordsPartitions, plugin, contribution);
    }

}
