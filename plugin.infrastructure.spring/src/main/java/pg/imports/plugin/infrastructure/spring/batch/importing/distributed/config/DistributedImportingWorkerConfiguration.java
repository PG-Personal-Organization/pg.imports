package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.DistributedImportPartitionResponseSender;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageRequest;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DistributedImportingWorkerConfiguration {
    private final PlatformTransactionManager chainedTransactionManager;
    private final JobRepository jobRepository;

    private final Tasklet partitionedImportingTasklet;
    private final JobExplorer jobExplorer;
    private final StepLocator stepLocator;

    private final MessageChannel partitionRequestInbound;

    private final DistributedImportPartitionResponseSender distributedImportPartitionResponseSender;

    @Bean
    public TaskletStep distributedImportingWorkerStep() {
        return new StepBuilder(DistributedImportingMasterConfiguration.WORKER_STEP, jobRepository)
                .tasklet(partitionedImportingTasklet, chainedTransactionManager)
                .build();
    }

    @Bean
    public StepExecutionRequestHandler stepExecutionRequestHandler() {
        StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
        handler.setJobExplorer(jobExplorer);
        handler.setStepLocator(stepLocator);
        return handler;
    }

    @Bean
    public IntegrationFlow inboundRequestsFlow() {
        return IntegrationFlow
                .from(partitionRequestInbound)
                .transform(ImportPartitionMessageRequest::getRequest)
                .handle(stepExecutionRequestHandler())
                .transform(ImportPartitionMessageResponse::new)
                .handle(distributedImportPartitionResponseSender)
                .get();
    }
}
