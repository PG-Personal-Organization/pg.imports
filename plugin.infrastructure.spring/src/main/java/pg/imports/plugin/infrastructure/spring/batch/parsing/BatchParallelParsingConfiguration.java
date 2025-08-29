package pg.imports.plugin.infrastructure.spring.batch.parsing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.api.parsing.ReaderOutputItem;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.parsing.listeners.ParsingErrorJobListener;
import pg.imports.plugin.infrastructure.spring.batch.parsing.listeners.SimpleParsingExecutionErrorListener;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.ReaderOutputItemProcessor;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriter;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager;
import pg.imports.plugin.infrastructure.spring.common.listeners.LoggingJobExecutionListener;

import java.util.List;
import java.util.concurrent.Future;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchParallelParsingConfiguration {
    private static final int PARSING_STEP_TRANSACTION_TIMEOUT = 1800;
    private static final int QUEUE_LIMIT = 500;

    private final TaskletStep initParsingStep;
    private final TaskletStep finishParsingStep;
    private final PluginCache pluginCache;
    private final JobRepository jobRepository;
    private final RecordsRepository recordsRepository;
    private final ImportRepository importRepository;
    private final EventSender eventSender;
    private final ItemReader<ReaderOutputItem<Object>> itemReader;
    private final PlatformTransactionManager chainedTransactionManager;
    private final List<RecordsWriter> recordsWriters;

    @Value("${pg.imports.batch.parallel.parsing.corePoolSize:4}")
    private int corePoolSize;

    @Value("${pg.imports.batch.parallel.parsing.maxPoolSize:12}")
    private int maxPoolSize;

    @Bean
    public TaskExecutor parallelParsingTaskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(corePoolSize);
        t.setMaxPoolSize(maxPoolSize);
        t.setQueueCapacity(Math.max(maxPoolSize * 2, QUEUE_LIMIT));
        t.setThreadNamePrefix("parallel-imports-parser-");
        t.initialize();
        return t;
    }

    @JobScope
    @Bean
    public TaskletStep parallelParsingStep(final @Value("#{jobExecution}") JobExecution jobExecution) {
        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(PARSING_STEP_TRANSACTION_TIMEOUT);

        var importContext = JobUtil.getImportContext(jobExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());
        var chunkSize = plugin.getChunkSize();

        return new StepBuilder("parallelParsingStep", jobRepository)
                .<ReaderOutputItem<Object>, Future<PartitionedRecord>>chunk(chunkSize, chainedTransactionManager)
                .reader(itemReader)
                .processor(asyncParallelItemProcessor())
                .writer(asyncParallelItemWriter())
                .transactionAttribute(transactionAttribute)
                .faultTolerant()
                .retryPolicy(new NeverRetryPolicy())
                .listener(new SimpleParsingExecutionErrorListener())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<PartitionedRecord> parallelItemWriter(final @Value("#{stepExecution}") StepExecution execution) {
        return new RecordsWriterManager(pluginCache, recordsWriters, recordsRepository, importRepository, execution);
    }

    @Bean
    public AsyncItemWriter<PartitionedRecord> asyncParallelItemWriter() {
        var w = new AsyncItemWriter<PartitionedRecord>();
        w.setDelegate(parallelItemWriter(null));
        return w;
    }

    @Bean
    @StepScope
    public ItemProcessor<ReaderOutputItem<Object>, PartitionedRecord> parallelItemProcessor(
            final @Value("#{stepExecution}") StepExecution execution) {
        return new ReaderOutputItemProcessor(execution, pluginCache);
    }

    @Bean
    public AsyncItemProcessor<ReaderOutputItem<Object>, PartitionedRecord> asyncParallelItemProcessor() {
        var p = new AsyncItemProcessor<ReaderOutputItem<Object>, PartitionedRecord>();
        p.setDelegate(parallelItemProcessor(null));
        p.setTaskExecutor(parallelParsingTaskExecutor());
        return p;
    }

    @Bean
    public Job localParallelParsingJob() {
        return new JobBuilder("localParallelParsingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ParsingErrorJobListener(eventSender, recordsRepository))
                .start(initParsingStep)
                .next(parallelParsingStep(null))
                .next(finishParsingStep)
                .build();
    }
}
