package pg.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.plugin.infrastructure.spring.batch.importing.partition.ImportingPartitioner;
import pg.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchParallelImportingConfiguration {
    private static final String WORKER_STEP_NAME = "parallelImportingWorkerStep";
    private static final String PARTITIONED_STEP_NAME = "parallelImportingStep";
    private static final int IMPORTING_STEP_TRANSACTION_TIMEOUT = 3600;

    @Value("${batch.parallel.importing.corePoolSize:4}")
    private int corePoolSize;

    @Value("${batch.parallel.importing.maxPoolSize:12}")
    private int maxPoolSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ImportingPartitioner importingPartitioner;

    private final Tasklet partitionedImportingTasklet;
    private final TaskletStep initImportingStep;
    private final TaskletStep finishImportingStep;

    @Bean
    public TaskletStep parallelImportingWorkerStep() {
        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(IMPORTING_STEP_TRANSACTION_TIMEOUT);

        return new StepBuilder(WORKER_STEP_NAME, jobRepository)
                .tasklet(partitionedImportingTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(parallelImportingWorkerStep());
        handler.setGridSize(corePoolSize);
        handler.setTaskExecutor(parallelImportTaskExecutor());
        return handler;
    }

    @Bean
    public Step parallelImportingStep() {
        return new StepBuilder(PARTITIONED_STEP_NAME, jobRepository)
                .partitioner(WORKER_STEP_NAME, importingPartitioner)
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Job localParallelImportingJob() {
        return new JobBuilder("localParallelImportingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .start(initImportingStep)
                .next(parallelImportingStep())
                .next(finishImportingStep)
                .build();
    }

    @Bean
    public TaskExecutor parallelImportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(maxPoolSize);
        executor.setThreadNamePrefix("parallel-imports-importer-");
        executor.initialize();
        return executor;
    }
}
