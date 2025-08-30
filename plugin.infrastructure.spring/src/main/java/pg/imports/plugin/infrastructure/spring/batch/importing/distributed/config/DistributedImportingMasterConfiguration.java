package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.DistributedImportPartitionRequestSender;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;
import pg.imports.plugin.infrastructure.spring.batch.importing.listeners.ImportingErrorJobListener;
import pg.imports.plugin.infrastructure.spring.batch.importing.partition.ImportingPartitioner;
import pg.imports.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;
import pg.kafka.sender.EventSender;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DistributedImportingMasterConfiguration {
    public static final String MANAGER_STEP = "distributedImportingStep";
    public static final String WORKER_STEP = "distributedImportingWorkerStep";
    private static final int GRID_SIZE = 6;
    private static final int POLL_MS = 3_000;

    private final JobExplorer jobExplorer;

    private final JobRepository jobRepository;

    private final MessageChannel importingRequests;
    private final PollableChannel importingReplies;
    private final MessageChannel partitionReplyInbound;

    private final DistributedImportPartitionRequestSender distributedImportPartitionRequestSender;

    private final TaskletStep initImportingStep;
    private final TaskletStep finishImportingStep;

    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;

    @Bean
    @StepScope
    public MessageChannelPartitionHandler distributedPartitionHandler() {
        MessageChannelPartitionHandler handler = new MessageChannelPartitionHandler();
        handler.setStepName(WORKER_STEP);
        handler.setGridSize(GRID_SIZE);
        handler.setMessagingOperations(new MessagingTemplate(importingRequests));
        handler.setReplyChannel(importingReplies);
        handler.setPollInterval(POLL_MS);
        handler.setJobExplorer(jobExplorer);
        return handler;
    }

    @Bean
    public Step distributedImportingStep(final @Lazy ImportingPartitioner importingPartitioner, final @Lazy MessageChannelPartitionHandler distributedPartitionHandler) {
        return new StepBuilder(MANAGER_STEP, jobRepository)
                .partitioner(WORKER_STEP, importingPartitioner)
                .partitionHandler(distributedPartitionHandler)
                .build();
    }

    @Bean
    public Job distributedImportingJob(final @Lazy Step distributedImportingStep) {
        return new JobBuilder("distributedImportingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ImportingErrorJobListener(eventSender, recordsRepository))
                .start(initImportingStep)
                .next(distributedImportingStep)
                .next(finishImportingStep)
                .build();
    }

    @Bean
    @Lazy(false)
    public IntegrationFlow importingOutboundRequestsFlow() {
        return IntegrationFlow
                .from(importingRequests)
                .handle(distributedImportPartitionRequestSender)
                .get();
    }

    @Bean
    @Lazy(false)
    public IntegrationFlow inboundPartitionReplies() {
        return IntegrationFlow
                .from(partitionReplyInbound)
                .<ImportPartitionMessageResponse, org.springframework.batch.core.StepExecution>transform(
                        response -> jobExplorer.getStepExecution(response.getJobExecutionId(), response.getStepExecutionId())
                )
                .channel(importingReplies)
                .get();
    }
}
