package pg.imports.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.spring.batch.importing.listeners.ImportingErrorJobListener;
import pg.imports.plugin.infrastructure.spring.batch.importing.listeners.SimpleImportingExecutionErrorListener;
import pg.imports.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class BatchLocalImportingConfiguration {
    private static final int IMPORTING_STEP_TRANSACTION_TIMEOUT = 3600;

    private final PlatformTransactionManager chainedTransactionManager;

    private final TaskletStep initImportingStep;
    private final TaskletStep finishImportingStep;
    private final Tasklet simpleImportingTasklet;

    private final JobRepository jobRepository;
    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;

    @Bean
    public Job simpleImportingJob() {
        return new JobBuilder("simpleImportingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ImportingErrorJobListener(eventSender, recordsRepository))
                .start(initImportingStep)
                .next(simpleImportingStep(null))
                .next(finishImportingStep)
                .build();
    }

    @JobScope
    @Bean
    public TaskletStep simpleImportingStep(final @Value("#{jobExecution}") JobExecution jobExecution) {
        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(IMPORTING_STEP_TRANSACTION_TIMEOUT);

        return new StepBuilder("simpleImportingStep", jobRepository)
                .tasklet(simpleImportingTasklet, chainedTransactionManager)
                .transactionAttribute(transactionAttribute)
                .listener(new SimpleImportingExecutionErrorListener())
                .allowStartIfComplete(false)
                .build();
    }

}
