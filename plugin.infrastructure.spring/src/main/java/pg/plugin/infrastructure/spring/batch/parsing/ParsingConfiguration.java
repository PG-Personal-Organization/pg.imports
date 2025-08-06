package pg.plugin.infrastructure.spring.batch.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.beanio.StreamFactory;
import org.beanio.builder.StreamBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.service.api.FileService;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.parsing.BeanIoReaderDefinition;
import pg.plugin.api.parsing.ReaderDefinition;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.persistence.records.db.RecordRepository;
import pg.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.plugin.infrastructure.spring.batch.parsing.readers.BeanIoReader;
import pg.plugin.infrastructure.spring.batch.parsing.tasklets.ParsingFinisherTasklet;
import pg.plugin.infrastructure.spring.batch.parsing.tasklets.ParsingInitializerTasklet;
import pg.plugin.infrastructure.spring.batch.parsing.writing.EngineDbRecordsWriter;
import pg.plugin.infrastructure.spring.batch.parsing.writing.EngineMongoRecordsWriter;
import pg.plugin.infrastructure.spring.batch.parsing.writing.PluginStoredRecordsWriter;
import pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriter;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;

import java.io.InputStream;

@Import({
        BatchLocalParsingConfiguration.class,
        BatchParallelParsingConfiguration.class,
        BatchDistributedParsingConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ParsingConfiguration {
    private static final int FINISH_PARSING_STEP_TRANSACTION_TIMEOUT = 60;
    private static final int INIT_PARSING_STEP_TRANSACTION_TIMEOUT = 60;

    private final PluginCache pluginCache;
    private final FileService fileService;
    private final JobRepository jobRepository;
    private final RecordsRepository recordsRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public ParsingJobLauncher parsingJobLauncher(final ImportsConfigProvider importsConfigProvider,
                                                 final JobLauncher jobLauncher,
                                                 final Job localParsingJob,
                                                 final Job localParallelParsingJob,
                                                 final Job distributedParsingJob) {
        return new ConfigurationBasedParsingJobLauncher(importsConfigProvider,
                jobLauncher,
                localParsingJob,
                localParallelParsingJob,
                distributedParsingJob);
    }

    @Bean
    public Tasklet parsingInitializerTasklet(final ImportRepository importRepository, final ImportsConfigProvider importsConfigProvider) {
        return new ParsingInitializerTasklet(importRepository, importsConfigProvider);
    }

    @Bean
    public TaskletStep initParsingStep(final Tasklet parsingInitializerTasklet) {
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(INIT_PARSING_STEP_TRANSACTION_TIMEOUT);
        return new StepBuilder("initParsingStep", jobRepository)
                .tasklet(parsingInitializerTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public Tasklet parsingFinisherTasklet(final ImportRepository importRepository, final EventSender eventSender) {
        return new ParsingFinisherTasklet(importRepository, recordsRepository, eventSender);
    }

    @Bean
    public TaskletStep finishParsingStep(final Tasklet parsingFinisherTasklet) {
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(FINISH_PARSING_STEP_TRANSACTION_TIMEOUT);
        return new StepBuilder("finishParsingStep", jobRepository)
                .tasklet(parsingFinisherTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public RecordsWriter engineDbRecordsWriter(final RecordRepository recordRepository, final ObjectMapper batchObjectMapper) {
        return new EngineDbRecordsWriter(recordRepository, batchObjectMapper);
    }

    @Bean
    public RecordsWriter engineMongoRecordsWriter(final MongoRecordRepository mongoRecordRepository, final ObjectMapper batchObjectMapper) {
        return new EngineMongoRecordsWriter(mongoRecordRepository, batchObjectMapper);
    }

    @Bean
    public RecordsWriter pluginStoredRecordsWriter() {
        return new PluginStoredRecordsWriter();
    }

    @Bean
    @StepScope
    public AbstractItemCountingItemStreamItemReader<ReaderOutputItem<Object>> itemReader(
            final @Value("#{stepExecution}") StepExecution stepExecution) {
        InputStream inputStream = fileService.getFileStream(JobUtil.getFileId(stepExecution));

        ImportContext importContext = JobUtil.getImportContext(stepExecution);
        ImportPlugin plugin = pluginCache.getPlugin(JobUtil.getPluginCode(stepExecution));
        ReaderDefinition readerDefinition = plugin.getParsingComponentProvider().getReaderDefinition();

        if (readerDefinition instanceof BeanIoReaderDefinition) {
            return buildBeanIoReader(stepExecution, inputStream, importContext, readerDefinition, plugin);
        }
        throw new IllegalStateException("Non supported readerDefinition = " + readerDefinition + ", importContext = " + importContext);
    }

    private AbstractItemCountingItemStreamItemReader<ReaderOutputItem<Object>> buildBeanIoReader(
            final StepExecution stepExecution,
            final InputStream inputStream,
            final ImportContext importContext,
            final ReaderDefinition readerDefinition,
            final ImportPlugin plugin
    ) {
        InputStreamResource res = new InputStreamResource(inputStream);
        BeanIoReader reader = new BeanIoReader(plugin, importContext, stepExecution);
        if (readerDefinition instanceof BeanIoReaderDefinition beanIoReaderDefinition
                && beanIoReaderDefinition.getCharset() != null) {
            reader.setEncoding(beanIoReaderDefinition.getCharset().name());
        }
        StreamBuilder streamBuilder = readerDefinition.getStreamBuilder();
        StreamFactory factory = StreamFactory.newInstance();
        factory.define(streamBuilder);
        reader.setStreamName(readerDefinition.getReaderName());
        reader.setStreamFactory(factory);
        reader.setResource(res);

        return reader;
    }
}
