package pg.imports.plugin.infrastructure.spring.batch.common.distributed;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@RequiredArgsConstructor
public class DistributedJobsExecutionListener implements JobExecutionListener {
    private final LocalJobRegistry localJobRegistry;

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        localJobRegistry.register(jobExecution.getId());
    }
    @Override
    public void afterJob(final JobExecution jobExecution) {
        localJobRegistry.unregister(jobExecution.getId());
    }
}
