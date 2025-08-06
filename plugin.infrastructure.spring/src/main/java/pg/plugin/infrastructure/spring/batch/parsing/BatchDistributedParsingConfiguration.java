package pg.plugin.infrastructure.spring.batch.parsing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.message.MessageDestination;
import pg.kafka.sender.EventSender;
import pg.kafka.topic.TopicName;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.plugin.infrastructure.spring.batch.parsing.distributed.*;
import pg.plugin.infrastructure.spring.batch.parsing.listeners.DistributedParsingErrorStepListener;
import pg.plugin.infrastructure.spring.batch.parsing.listeners.ParsingErrorJobListener;
import pg.plugin.infrastructure.spring.batch.parsing.processor.DistributedOutputItemProcessor;
import pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriter;
import pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager;
import pg.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;

import java.util.List;

@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchDistributedParsingConfiguration {
    private static final int PARSING_STEP_TRANSACTION_TIMEOUT = 3600;

    private final Environment environment;
    private final TaskletStep initParsingStep;
    private final TaskletStep finishParsingStep;
    private final PluginCache pluginCache;
    private final JobRepository jobRepository;
    private final EventSender eventSender;
    private final RecordsRepository recordsRepository;
    private final ImportRepository importRepository;
    private final ItemReader<ReaderOutputItem<Object>> itemReader;
    private final PlatformTransactionManager chainedTransactionManager;
    private final List<RecordsWriter> recordsWriters;

    @Bean
    public MessageDestination parsingChunkMessageRequestDestination() {
        var applicationName = environment.getProperty("spring.application.name");
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-chunk-request-processing-batch-topic"))
                .messageClass(ParseChunkMessageRequest.class)
                .build();
    }

    @Bean
    public MessageDestination parsingChunkMessageResponseDestination() {
        var applicationName = environment.getProperty("spring.application.name");
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-chunk-response-processing-batch-topic"))
                .messageClass(ParseChunkMessageResponse.class)
                .build();
    }

    @Bean
    public MessageChannel parsingRequests() {
        return new DirectChannel();
    }

    @Bean
    public PollableChannel parsingReplies() {
        return new QueueChannel();
    }

    @Bean
    public IntegrationFlow parsingOutboundFlow(final DistributedParseChunkSender distributedParseChunkSender) {
        return IntegrationFlow
                .from(parsingRequests())
                .handle(distributedParseChunkSender)
                .get();
    }

    @Bean
    public IntegrationFlow parsingInboundReplies() {
        return IntegrationFlow
                .fromSupplier(this::distributedChunkHandler)
                .channel(parsingReplies())
                .get();
    }

    @Bean
    @StepScope
    public DistributedParseChunkSender distributedChunkSender(final @Value("#{stepExecution.jobExecution.jobId}") long jobId) {
        return new DistributedParseChunkSender(eventSender, jobId);
    }

    @Bean
    public DistributedParseChunkRequestHandler distributedChunkHandler() {
        return new DistributedParseChunkRequestHandler(distributedChunkItemWriter(null), distributedItemProcessor(null), eventSender);
    }

    @Bean
    public DistributedParseChunkResponseHandler distributedChunkResponseHandler() {
        return new DistributedParseChunkResponseHandler(parsingReplies());
    }

    @Bean
    @StepScope
    public DistributedOutputItemProcessor distributedItemProcessor(
            final @Value("#{stepExecution}") StepExecution execution) {
        return new DistributedOutputItemProcessor(execution, pluginCache);
    }

    @Bean
    @StepScope
    public RecordsWriterManager distributedChunkItemWriter(final @Value("#{stepExecution}") StepExecution execution) {
        return new RecordsWriterManager(execution, pluginCache, recordsWriters, recordsRepository, importRepository);
    }

    @JobScope
    @Bean
    public TaskletStep distributedParsingStep(final @Value("#{jobExecution}") JobExecution jobExecution) {
        var importContext = JobUtil.getImportContext(jobExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(PARSING_STEP_TRANSACTION_TIMEOUT);

        var chunkSize = plugin.getChunkSize();

        var stepBuilder = new RemoteChunkingManagerStepBuilder<>("distributedParsingStep", jobRepository)
                .retryPolicy(new NeverRetryPolicy())
                .transactionManager(chainedTransactionManager)
                .transactionAttribute(transactionAttribute)
                .chunk(chunkSize)
                .reader(itemReader)
                .outputChannel(parsingRequests())
                .inputChannel(parsingReplies())
                .listener(new DistributedParsingErrorStepListener(recordsRepository));
        return new FaultTolerantStepBuilder<>(stepBuilder).build();
    }

    @Bean
    public Job distributedParsingJob() {
        return new JobBuilder("distributedParsingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ParsingErrorJobListener(eventSender, recordsRepository))
                .start(initParsingStep)
                .next(distributedParsingStep(null))
                .next(finishParsingStep)
                .build();
    }
}
