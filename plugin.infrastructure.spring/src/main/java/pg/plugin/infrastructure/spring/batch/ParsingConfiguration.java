package pg.plugin.infrastructure.spring.batch;

import lombok.RequiredArgsConstructor;
import org.beanio.StreamFactory;
import org.beanio.builder.StreamBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import pg.lib.awsfiles.service.api.FileService;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.parsing.BeanIoReaderDefinition;
import pg.plugin.api.parsing.ReaderDefinition;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.parsing.*;
import pg.plugin.infrastructure.spring.batch.parsing.readers.BeanIoReader;
import pg.plugin.infrastructure.spring.batch.parsing.tasklets.ParsingInitializerTasklet;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;

import java.io.InputStream;

@Import({
        BatchLocalParsingConfiguration.class,
        BatchParallelParsingConfiguration.class,
        BatchDistributedParsingConfiguration.class,
        BatchDistributedParallelParsingConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ParsingConfiguration {
    private final PluginCache pluginCache;
    private final FileService fileService;

    @Bean
    public ParsingJobLauncher parsingJobLauncher(final ImportsConfigProvider importsConfigProvider,
                                                 final JobLauncher jobLauncher,
                                                 final Job localParsingJob,
                                                 final Job localParallelParsingJob,
                                                 final Job distributedParallelParsingJob,
                                                 final Job distributedParsingJob) {
        return new ConfigurationBasedParsingJobLauncher(importsConfigProvider, jobLauncher, localParsingJob, localParallelParsingJob, distributedParallelParsingJob,
                distributedParsingJob);
    }

    @Bean
    public Tasklet parsingInitializerTasklet(final ImportRepository importRepository, final ImportsConfigProvider importsConfigProvider) {
        return new ParsingInitializerTasklet(importRepository, importsConfigProvider);
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
