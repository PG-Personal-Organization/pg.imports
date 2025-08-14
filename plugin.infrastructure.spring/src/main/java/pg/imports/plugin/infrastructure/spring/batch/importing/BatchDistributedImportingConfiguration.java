package pg.imports.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
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
import pg.kafka.topic.TopicName;

@Configuration
@Import({
        DistributedImportingMasterConfiguration.class,
        DistributedImportingWorkerConfiguration.class
})
@EnableBatchProcessing
@EnableBatchIntegration
@RequiredArgsConstructor(onConstructor_ = @__({@Autowired, @Lazy}))
public class BatchDistributedImportingConfiguration {
    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;
    private final Environment environment;

    @Bean
    public MessageDestination importingChunkMessageRequestDestination() {
        var applicationName = environment.getProperty("spring.application.name");
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-chunk-request-importing-batch-topic"))
                .messageClass(ImportPartitionMessageRequest.class)
                .build();
    }

    @Bean
    public DistributedImportPartitionResponseMessageHandler distributedImportPartitionResponseMessageHandler() {
        return new DistributedImportPartitionResponseMessageHandler(importingReplies());
    }

    @Bean
    public MessageDestination importingChunkMessageResponseDestination() {
        var applicationName = environment.getProperty("spring.application.name");
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
    public MessageChannel partitionReplyInbound() {
        return new DirectChannel();
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
}
