package pg.imports.plugin.infrastructure.spring.batch.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.imports.plugin.api.parsing.ReaderOutputItem;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.parsing.distributed.*;
import pg.imports.plugin.infrastructure.spring.batch.parsing.listeners.DistributedParsingErrorStepListener;
import pg.imports.plugin.infrastructure.spring.batch.parsing.listeners.ParsingErrorJobListener;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.DistributedOutputItemProcessor;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriter;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager;
import pg.imports.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;
import pg.kafka.message.MessageDestination;
import pg.kafka.sender.EventSender;
import pg.kafka.topic.TopicName;

import java.util.List;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @__({@Autowired, @Lazy}))
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
    private final PlatformTransactionManager chainedTransactionManager;
    private final List<RecordsWriter> recordsWriters;
    private final ObjectMapper batchObjectMapper;

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
    public DistributedParseChunkSender distributedChunkSender() {
        return new DistributedParseChunkSender(eventSender, batchObjectMapper);
    }

    @Bean
    public DistributedParseChunkRequestHandler distributedChunkHandler() {
        return new DistributedParseChunkRequestHandler(distributedChunkItemWriter(), distributedItemProcessor(), eventSender, batchObjectMapper, pluginCache);
    }

    @Bean
    public DistributedParseChunkResponseHandler distributedChunkResponseHandler() {
        return new DistributedParseChunkResponseHandler(parsingReplies());
    }

    @Bean
    public DistributedOutputItemProcessor distributedItemProcessor() {
        return new DistributedOutputItemProcessor(pluginCache);
    }

    @Bean
    public RecordsWriterManager distributedChunkItemWriter() {
        return new RecordsWriterManager(pluginCache, recordsWriters, recordsRepository, importRepository);
    }

    @JobScope
    @Bean
    public TaskletStep distributedParsingStep(final @Value("#{jobExecution}") JobExecution jobExecution,
                                              final ItemReader<ReaderOutputItem<Object>> itemReader
    ) {
        var importContext = JobUtil.getImportContext(jobExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(PARSING_STEP_TRANSACTION_TIMEOUT);

        var chunkSize = plugin.getChunkSize();

        return new RemoteChunkingManagerStepBuilder<ReaderOutputItem<Object>, PartitionedRecord>("distributedParsingStep", jobRepository)
                .transactionManager(chainedTransactionManager)
                .transactionAttribute(transactionAttribute)
                .chunk(chunkSize)
                .reader(itemReader)
                .outputChannel(parsingRequests())
                .inputChannel(parsingReplies())
                .listener(new DistributedParsingErrorStepListener(recordsRepository))
                .build();
    }

    @Bean
    public Job distributedParsingJob(final ItemReader<ReaderOutputItem<Object>> itemReader) {
        return new JobBuilder("distributedParsingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ParsingErrorJobListener(eventSender, recordsRepository))
                .start(initParsingStep)
                .next(distributedParsingStep(null, itemReader))
                .next(finishParsingStep)
                .build();
    }
}
