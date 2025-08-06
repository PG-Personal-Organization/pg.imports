package pg.plugin.infrastructure.spring.batch.importing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import pg.kafka.sender.EventSender;
import pg.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.persistence.records.db.RecordRepository;
import pg.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.importing.readers.LibraryJsonImportingRecordsProvider;
import pg.plugin.infrastructure.spring.batch.importing.readers.MongoImportingRecordsProvider;
import pg.plugin.infrastructure.spring.batch.importing.tasklets.ImportingFinisherTasklet;
import pg.plugin.infrastructure.spring.batch.importing.tasklets.ImportingInitializerTasklet;
import pg.plugin.infrastructure.spring.batch.importing.tasklets.PartitionedImportingTasklet;
import pg.plugin.infrastructure.spring.batch.importing.tasklets.SimpleImportingTasklet;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;

@Import({
        BatchLocalImportingConfiguration.class,
        BatchParallelImportingConfiguration.class,
        BatchDistributedImportingConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ImportingConfiguration {
    private static final int FINISH_IMPORTING_STEP_TRANSACTION_TIMEOUT = 1800;
    private static final int INIT_IMPORTING_STEP_TRANSACTION_TIMEOUT = 900;

    private final ImportRepository importRepository;
    private final ImportsConfigProvider importsConfigProvider;
    private final JobRepository jobRepository;
    private final PluginCache pluginCache;
    private final RecordsRepository recordsRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public ImportingJobLauncher importingJobLauncher(final ImportsConfigProvider importsConfigProvider,
                                                     final JobLauncher jobLauncher,
                                                     final Job localImportingJob,
                                                     final Job localParallelImportingJob,
                                                     final Job distributedImportingJob
    ) {
        return new ConfigurationBasedImportingJobLauncher(importsConfigProvider,
                jobLauncher,
                localImportingJob,
                localParallelImportingJob,
                distributedImportingJob);
    }

    @Bean
    public Tasklet importingInitializerTasklet() {
        return new ImportingInitializerTasklet(importRepository, importsConfigProvider);
    }

    @Bean
    public Tasklet simpleImportingTasklet(final LibraryJsonImportingRecordsProvider dbJsonRecordsProvider, final MongoImportingRecordsProvider mongoRecordsProvider) {
        return new SimpleImportingTasklet(importRepository, pluginCache, recordsRepository, dbJsonRecordsProvider, mongoRecordsProvider);
    }

    @Bean
    public Tasklet partitionedImportingTasklet(final LibraryJsonImportingRecordsProvider dbJsonRecordsProvider, final MongoImportingRecordsProvider mongoRecordsProvider) {
        return new PartitionedImportingTasklet(importRepository, pluginCache, recordsRepository, dbJsonRecordsProvider, mongoRecordsProvider);
    }

    @Bean
    public TaskletStep initImportingStep(final Tasklet importingInitializerTasklet) {
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(INIT_IMPORTING_STEP_TRANSACTION_TIMEOUT);
        return new StepBuilder("initImportingStep", jobRepository)
                .tasklet(importingInitializerTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public Tasklet importingFinisherTasklet(final EventSender eventSender) {
        return new ImportingFinisherTasklet(importRepository, recordsRepository, eventSender);
    }

    @Bean
    public TaskletStep finishImportingStep(final Tasklet importingFinisherTasklet) {
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setTimeout(FINISH_IMPORTING_STEP_TRANSACTION_TIMEOUT);
        return new StepBuilder("finishImportingStep", jobRepository)
                .tasklet(importingFinisherTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public LibraryJsonImportingRecordsProvider dbJsonRecordsProvider(final RecordRepository recordRepository) {
        return new LibraryJsonImportingRecordsProvider(recordRepository);
    }

    @Bean
    public MongoImportingRecordsProvider mongoRecordsProvider(final MongoRecordRepository recordRepository, final ObjectMapper batchObjectMapper) {
        return new MongoImportingRecordsProvider(recordRepository, batchObjectMapper);
    }

//    @Bean
//    public ImportingErrorHandler importingErrorHandler() {
//        return new ImportingErrorHandler(recordsRepository, pluginCache);
//    }

}
