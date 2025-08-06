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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import pg.plugin.infrastructure.spring.batch.importing.partition.ImportingPartitioner;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchParallelImportingConfiguration {
    private static final String WORKER_STEP_NAME = "parallelImportingWorkerStep";
    private static final String PARTITIONED_STEP_NAME = "parallelImportingStep";
    private static final int GRID_SIZE = 6; // Number of partitions/threads

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ImportingPartitioner importingPartitioner;

    private final Tasklet partitionedImportingTasklet;
    private final TaskletStep initImportingStep;
    private final TaskletStep finishImportingStep;

    @Bean
    public TaskletStep parallelImportingWorkerStep() {
        return new StepBuilder(WORKER_STEP_NAME, jobRepository)
                .tasklet(partitionedImportingTasklet, transactionManager)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(parallelImportingWorkerStep());
        handler.setGridSize(GRID_SIZE);
        handler.setTaskExecutor(importTaskExecutor());
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
                .start(initImportingStep)
                .next(parallelImportingStep())
                .next(finishImportingStep)
                .build();
    }

    @Bean
    public TaskExecutor importTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setQueueCapacity(GRID_SIZE);
        executor.setThreadNamePrefix("importer-");
        executor.initialize();
        return executor;
    }
}
