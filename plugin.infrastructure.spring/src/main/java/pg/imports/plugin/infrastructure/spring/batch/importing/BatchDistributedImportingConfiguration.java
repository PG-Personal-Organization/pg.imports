package pg.imports.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.DistributedResponseConsumerGroupProvider;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.LocalJobRegistry;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.config.DistributedImportingMasterConfiguration;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.config.DistributedImportingWorkerConfiguration;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.DistributedImportPartitionRequestSender;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.DistributedImportPartitionResponseSender;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageRequest;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.kafka.DistributedImportPartitionRequestMessageHandler;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.kafka.DistributedImportPartitionResponseMessageHandler;
import pg.imports.plugin.infrastructure.spring.batch.importing.partition.ImportingPartitioner;
import pg.kafka.message.MessageDestination;
import pg.kafka.sender.EventSender;
import pg.kafka.topic.TopicDefinition;
import pg.kafka.topic.TopicName;

@Configuration
@Import({
        DistributedImportingMasterConfiguration.class,
        DistributedImportingWorkerConfiguration.class
})
@RequiredArgsConstructor(onConstructor_ = @__({@Autowired, @Lazy}))
public class BatchDistributedImportingConfiguration {
    private static final int DEFAULT_PARTITIONS = 16;

    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;
    private final Environment environment;
    private final JobExplorer jobExplorer;

    @Bean
    public TopicDefinition importingChunkMessageRequestTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-chunk-request-importing-batch-topic"))
                .partitions(DEFAULT_PARTITIONS)
                .build();
    }

    @Bean
    public MessageDestination importingChunkMessageRequestDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-chunk-request-importing-batch-topic"))
                .messageClass(ImportPartitionMessageRequest.class)
                .build();
    }

    @Bean
    public DistributedImportPartitionResponseMessageHandler distributedImportPartitionResponseMessageHandler(
            final MessageChannel importingRepliesBus,
            final DistributedResponseConsumerGroupProvider distributedResponseConsumerGroupProvider) {
        return new DistributedImportPartitionResponseMessageHandler(importingRepliesBus, distributedResponseConsumerGroupProvider);
    }

    @Bean
    public TopicDefinition importingChunkMessageResponseTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-chunk-response-importing-batch-topic"))
                .partitions(DEFAULT_PARTITIONS)
                .build();
    }

    @Bean
    public MessageDestination importingChunkMessageResponseDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-chunk-response-importing-batch-topic"))
                .messageClass(ImportPartitionMessageResponse.class)
                .build();
    }

    @Bean
    public DistributedImportPartitionRequestMessageHandler distributedImportPartitionRequestMessageHandler(
            final StepExecutionRequestHandler stepExecutionRequestHandler
    ) {
        return new DistributedImportPartitionRequestMessageHandler(eventSender, stepExecutionRequestHandler);
    }

    @Bean
    public MessageChannel importingRequests() {
        return new DirectChannel();
    }

    @Bean
    public PollableChannel importingReplies() {
        return new QueueChannel();
    }

    @Bean
    @Lazy(false)
    public IntegrationFlow importingRepliesRoutingFlow(final LocalJobRegistry localJobRegistry,
                                                     final @Lazy PollableChannel importingReplies,
                                                     final MessageChannel importingRepliesBus) {
        return IntegrationFlow.from(importingRepliesBus)
                .filter(org.springframework.messaging.Message.class, msg -> {
                    Long jobId = msg.getHeaders().get("jobExecutionId", Long.class);
                    return jobId != null && localJobRegistry.isLocal(jobId);
                })
                .transform(org.springframework.messaging.Message.class, Message::getPayload)
                .<ImportPartitionMessageResponse, StepExecution>transform(
                        resp -> jobExplorer.getStepExecution(resp.getJobExecutionId(), resp.getStepExecutionId())
                )
                .channel(importingReplies)
                .get();
    }

    @Bean
    public MessageChannel importingRepliesBus() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel partitionRequestInbound() {
        return new DirectChannel();
    }

    @Bean
    public DistributedImportPartitionRequestSender distributedImportPartitionRequestSender() {
        return new DistributedImportPartitionRequestSender(eventSender);
    }

    @Bean
    public DistributedImportPartitionResponseSender distributedImportPartitionResponseSender() {
        return new DistributedImportPartitionResponseSender(eventSender);
    }

    @Bean
    @JobScope
    public ImportingPartitioner importingPartitioner(final @Value("#{jobExecution}") JobExecution jobExecution) {
        var importContext = JobUtil.getImportContext(jobExecution);
        return new ImportingPartitioner(recordsRepository, importContext.getImportId());
    }

    private String getApplicationName() {
        return environment.getProperty("spring.application.name");
    }
}
