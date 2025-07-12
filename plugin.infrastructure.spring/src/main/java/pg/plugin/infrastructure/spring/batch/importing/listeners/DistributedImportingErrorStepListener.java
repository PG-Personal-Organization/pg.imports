package pg.plugin.infrastructure.spring.batch.importing.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportingErrorStepListener extends SimpleImportingExecutionErrorListener {
    private final RecordsRepository recordsRepository;

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        super.afterStep(stepExecution);

// TODO check if something is needed for distributed strategy
//        if (stepExecution.getStatus().isUnsuccessful()) {
//            log.info("Step execution {} unsuccessful", stepExecution.getStepName());
//            var importContext = JobUtil.getImportContext(stepExecution);
//            var recordChunks = recordsRepository.findAllByParentImportId(importContext.getImportId());
//            recordChunks.stream().map()
//        }
        return stepExecution.getExitStatus();
    }
}
