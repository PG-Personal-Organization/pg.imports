package pg.imports.plugin.infrastructure.spring.batch.parsing.listeners;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Log4j2
public class SimpleParsingExecutionErrorListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        if (!stepExecution.getFailureExceptions().isEmpty()) {
            log.error("There were {} exceptions.", stepExecution.getFailureExceptions().size());
            for (final Throwable throwable : stepExecution.getFailureExceptions()) {
                log.error("Exception occurred in partition step.", throwable);
            }
        }
        return stepExecution.getExitStatus();
    }
}
