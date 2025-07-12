package pg.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.data.PluginCode;
import pg.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.plugin.infrastructure.spring.batch.JobUtil;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;
import pg.plugin.infrastructure.states.AfterParsingImport;

@Log4j2
@RequiredArgsConstructor
public class ConfigurationBasedImportingJobLauncher implements ImportingJobLauncher {
    private final ImportsConfigProvider importsConfigProvider;
    private final JobLauncher jobLauncher;

    private final Job localImportingJob;
    private final Job localParallelImportingJob;
    private final Job distributedParallelImportingJob;
    private final Job distributedImportingJob;

    @Override
    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    @Transactional(propagation = Propagation.NEVER)
    public void launchImportingJob(final ImportPlugin importPlugin, final AfterParsingImport afterParsingImport) {
        try {
            JobExecution jobExecution;
            final var defaultJobParameters = defaultJobParameters(afterParsingImport.getImportId(), afterParsingImport.getPluginCode());
            switch (importsConfigProvider.getImportingStrategy()) {
                case LOCAL -> {
                    jobExecution = jobLauncher.run(localImportingJob, defaultJobParameters);
                    log.info("LocalParsingJob launched with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                case LOCAL_PARALLEL -> {
                    jobExecution = jobLauncher.run(localParallelImportingJob, defaultJobParameters);
                    log.info("LocalParallelParsingJob launched with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                case DISTRIBUTED -> {
                    jobExecution = jobLauncher.run(distributedImportingJob, defaultJobParameters);
                    log.info("DistributedParsingJob launched with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                default -> throw new IllegalArgumentException("Unknown importing strategy: " + importsConfigProvider.getImportingStrategy());
            }
        } catch (final Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private JobParameters defaultJobParameters(final ImportId importId, final PluginCode pluginCode) {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString(JobUtil.IMPORT_ID_KEY, importId.id());
        paramsBuilder.addString(JobUtil.PLUGIN_CODE_KEY, pluginCode.code());
        return paramsBuilder.toJobParameters();
    }
}
