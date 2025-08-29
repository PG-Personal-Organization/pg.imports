package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.importing.records.provider.ImportingRecordsProviderFactory;
import pg.imports.plugin.infrastructure.spring.batch.importing.tasklets.writer.ImportingErrorsWriterManager;

@Log4j2
public class SimpleImportingTasklet extends ImportingTasklet {

    public SimpleImportingTasklet(final ImportRepository importRepository,
                                  final PluginCache pluginCache,
                                  final RecordsRepository recordsRepository,
                                  final ImportingRecordsProviderFactory importingRecordsProviderFactory,
                                  final ImportingErrorsWriterManager importingErrorsWriterManager) {
        super(importRepository, pluginCache, recordsRepository, importingRecordsProviderFactory, importingErrorsWriterManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) {
        log.info("SimpleImportingTasklet started");
        var importContext = JobUtil.getImportContext(contribution.getStepExecution());
        var importId = importContext.getImportId();

        importRepository.getImportingImport(importId.id());
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        var recordsPartitions = recordsRepository.findAllByParentImportId(importId.id());
        return execute(recordsPartitions, plugin, contribution);
    }

}
