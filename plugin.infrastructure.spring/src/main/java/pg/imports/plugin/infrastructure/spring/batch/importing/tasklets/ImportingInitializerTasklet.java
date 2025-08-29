package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Transactional;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.config.KafkaImportsMessageStrategy;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.states.OngoingImportingImport;

@Log4j2
@RequiredArgsConstructor
public class ImportingInitializerTasklet implements Tasklet {
    private final ImportRepository importRepository;
    private final ImportsConfigProvider importsConfigProvider;

    @Override
    @Transactional
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) {
        ImportId importId = JobUtil.getImportId(contribution);
        ImportEntity ongoingImport = importRepository.getImportingImport(importId.id());
        PluginCode pluginCode = JobUtil.getPluginCode(contribution);
        storeParameters(pluginCode, contribution, ongoingImport);
        log.info("ImportInitializer {} finished", ongoingImport);
        return RepeatStatus.FINISHED;
    }

    private void storeParameters(final @NonNull PluginCode pluginCode, final StepContribution contribution, final OngoingImportingImport ongoingImport) {
        KafkaImportsMessageStrategy kafkaImportsMessageStrategy = importsConfigProvider.getKafkaMessage(pluginCode);
        RecordsStoringStrategy recordsStoringStrategy = importsConfigProvider.getRecordsStoring(pluginCode);
        log.info("KafkaImportsMessageStrategy: {}, RecordsStoringStrategy: {} resolved for import: {}",
                kafkaImportsMessageStrategy, recordsStoringStrategy, ongoingImport.getImportId());
        JobUtil.putKafkaImportsMessageStrategy(contribution, kafkaImportsMessageStrategy);
        JobUtil.putFileId(contribution, ongoingImport.getFileId());
        JobUtil.putImportContext(contribution, ImportContext.of(ongoingImport.getImportId(), ongoingImport.getPluginCode(), ongoingImport.getFileId(), recordsStoringStrategy));
    }
}
