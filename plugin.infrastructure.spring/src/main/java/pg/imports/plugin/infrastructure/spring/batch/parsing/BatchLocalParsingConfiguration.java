package pg.imports.plugin.infrastructure.spring.batch.parsing;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.api.parsing.ReaderOutputItem;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
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

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchLocalParsingConfiguration {
    private static final int PARSING_STEP_TRANSACTION_TIMEOUT = 1800;

    private final TaskletStep initParsingStep;
    private final TaskletStep finishParsingStep;
    private final PluginCache pluginCache;
    private final JobRepository jobRepository;
    private final RecordsRepository recordsRepository;
    private final ImportRepository importRepository;
    private final EventSender eventSender;
    private final ItemReader<ReaderOutputItem<Object>> itemReader;
    private final List<RecordsWriter> recordsWriters;

    @JobScope
    @Bean
    public TaskletStep simpleParsingStep(final @Value("#{jobExecution}") JobExecution jobExecution) {
        FaultTolerantStepBuilder<ReaderOutputItem<Object>, PartitionedRecord> faultTolerantStepBuilder = new FaultTolerantStepBuilder<>(
                new StepBuilder("simpleParsingStep", jobRepository));

        var transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(PARSING_STEP_TRANSACTION_TIMEOUT);

        var importContext = JobUtil.getImportContext(jobExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        return faultTolerantStepBuilder
                .retryPolicy(new NeverRetryPolicy())
                .chunk(plugin.getChunkSize())
                .reader(itemReader)
                .processor(simpleItemProcessor(null))
                .writer(simpleItemWriter(null))
                .transactionAttribute(transactionAttribute)
                .listener(new SimpleParsingExecutionErrorListener())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<PartitionedRecord> simpleItemWriter(final @Value("#{stepExecution}") StepExecution execution) {
        return new RecordsWriterManager(execution, pluginCache, recordsWriters, recordsRepository, importRepository);
    }

    @Bean
    @StepScope
    public ItemProcessor<? super ReaderOutputItem<Object>, PartitionedRecord> simpleItemProcessor(
            final @Value("#{stepExecution}") StepExecution execution) {
        return new ReaderOutputItemProcessor(execution, pluginCache);
    }

    @Bean
    public Job localParsingJob() {
        return new JobBuilder("localParsingJob", jobRepository)
                .listener(new LoggingJobExecutionListener())
                .listener(new ParsingErrorJobListener(eventSender, recordsRepository))
                .start(initParsingStep)
                .next(simpleParsingStep(null))
                .next(finishParsingStep)
                .build();
    }
}
