package pg.imports.plugin.infrastructure.spring.batch.parsing.tasklets;

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
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.common.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.spring.common.config.KafkaImportsMessageStrategy;
import pg.imports.plugin.infrastructure.states.OngoingParsingImport;

@Log4j2
@RequiredArgsConstructor
public class ParsingInitializerTasklet implements Tasklet {
    private final ImportRepository importRepository;
    private final ImportsConfigProvider importsConfigProvider;

    @Override
    @Transactional
    public RepeatStatus execute(final @NonNull StepContribution contribution, @NonNull final ChunkContext chunkContext) {
        ImportId importId = JobUtil.getImportId(contribution);
        ImportEntity scheduledImport = importRepository.getParsingImport(importId.id());
        PluginCode pluginCode = JobUtil.getPluginCode(contribution);
        storeParameters(pluginCode, contribution, scheduledImport);
        return RepeatStatus.FINISHED;
    }

    private void storeParameters(final @NonNull PluginCode pluginCode, final StepContribution contribution, final OngoingParsingImport ongoingParsingImport) {
        KafkaImportsMessageStrategy kafkaImportsMessageStrategy = importsConfigProvider.getKafkaMessage(pluginCode);
        RecordsStoringStrategy recordsStoringStrategy = importsConfigProvider.getRecordsStoring(pluginCode);
        log.info("KafkaImportsMessageStrategy: {}, RecordsStoringStrategy: {} resolved for import: {}",
                kafkaImportsMessageStrategy, recordsStoringStrategy, ongoingParsingImport.getImportId());
        JobUtil.putKafkaImportsMessageStrategy(contribution, kafkaImportsMessageStrategy);
        JobUtil.putRecordsStoringStrategy(contribution, recordsStoringStrategy);
        JobUtil.putFileId(contribution, ongoingParsingImport.getFileId());
        JobUtil.putImportContext(contribution, ImportContext.of(ongoingParsingImport.getImportId(), pluginCode, ongoingParsingImport.getFileId()));
    }

}
