package pg.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;

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
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());
        // TODO
        var recordsPartitions = recordsRepository.findAllByParentImportId(importId);
        var recordImporter = plugin.getImportingComponentsProvider().getRecordImporter();


        log.info("ImportingTasklet finished");
        return RepeatStatus.FINISHED;
    }
}
