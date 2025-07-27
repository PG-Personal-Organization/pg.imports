package pg.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.sender.EventSender;
import pg.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.processing.events.CompletedImportEvent;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;

@Log4j2
@RequiredArgsConstructor
public class ImportingFinisherTasklet implements Tasklet {
    private final ImportRepository importRepository;
    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;

    @Override
    @Transactional
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        var importContext = JobUtil.getImportContext(contribution.getStepExecution());
        var importId = importContext.getImportId();

        ImportEntity importingImport = importRepository.getImportingImport(importId.id());
        importingImport.finishImporting();
        importRepository.save(importingImport);

        // TODO importing finisher to implement if necessary

        eventSender.sendEvent(CompletedImportEvent.of(importId));
        log.info("ImportingFinisher {} finished", importId);
        return RepeatStatus.FINISHED;
    }
}
