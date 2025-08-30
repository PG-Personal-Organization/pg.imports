package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

    private final MessageChannel partitionRequestInbound;

    private final DistributedImportPartitionResponseSender distributedImportPartitionResponseSender;

    @Bean
    public TaskletStep distributedImportingWorkerStep() {
        return new StepBuilder(DistributedImportingMasterConfiguration.WORKER_STEP, jobRepository)
                .tasklet(partitionedImportingTasklet, chainedTransactionManager)
                .build();
    }

    @Bean
    public StepLocator workerStepLocator(final BeanFactory beanFactory) {
        BeanFactoryStepLocator locator = new BeanFactoryStepLocator();
        locator.setBeanFactory(beanFactory);
        return locator;
    }

    @Bean
    public StepExecutionRequestHandler stepExecutionRequestHandler(final StepLocator workerStepLocator) {
        StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
        handler.setJobExplorer(jobExplorer);
        handler.setStepLocator(workerStepLocator);
        return handler;
    }

    @Bean
    @Lazy(false)
    public IntegrationFlow inboundRequestsFlow(final StepExecutionRequestHandler stepExecutionRequestHandler) {
        return IntegrationFlow
                .from(partitionRequestInbound)
                .transform(ImportPartitionMessageRequest::getRequest)
                .handle(stepExecutionRequestHandler)
                .transform(execution -> {
                    var stepExecution = (StepExecution) execution;
                    return new ImportPartitionMessageResponse(stepExecution.getJobExecutionId(), stepExecution.getId());
                })
                .handle(distributedImportPartitionResponseSender)
                .get();
    }
}
