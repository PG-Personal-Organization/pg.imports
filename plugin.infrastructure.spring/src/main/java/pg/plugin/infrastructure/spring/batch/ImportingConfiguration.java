package pg.plugin.infrastructure.spring.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.plugin.infrastructure.spring.batch.importing.*;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;

@Import({
        BatchLocalImportingConfiguration.class,
        BatchParallelImportingConfiguration.class,
        BatchDistributedImportingConfiguration.class,
        BatchDistributedParallelImportingConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ImportingConfiguration {


    @Bean
    public ImportingJobLauncher importingJobLauncher(final ImportsConfigProvider importsConfigProvider,
                                                     final JobLauncher jobLauncher,
                                                     final Job localImportingJob,
                                                     final Job localParallelImportingJob,
//                                                     final Job distributedParallelImportingJob,
                                                     final Job distributedImportingJob
                                                     ) {
        return new ConfigurationBasedImportingJobLauncher(importsConfigProvider,
                jobLauncher,
                localImportingJob,
                localParallelImportingJob,
                null,
                distributedImportingJob);
    }

}
