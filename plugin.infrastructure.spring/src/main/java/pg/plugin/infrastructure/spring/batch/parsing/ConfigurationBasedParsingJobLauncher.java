package pg.plugin.infrastructure.spring.batch.parsing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.PluginCode;
import pg.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.plugin.infrastructure.spring.batch.JobUtil;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;
import pg.plugin.infrastructure.states.InParsingImport;

@Log4j2
@RequiredArgsConstructor
public class ConfigurationBasedParsingJobLauncher implements ParsingJobLauncher {
    private final ImportsConfigProvider importsConfigProvider;
    private final JobLauncher jobLauncher;

    private final Job localParsingJob;
    private final Job localParallelParsingJob;
    private final Job distributedParallelParsingJob;
    private final Job distributedParsingJob;

    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    @Transactional(propagation = Propagation.NEVER)
    public void launchParsingJob(final ImportPlugin importPlugin, final InParsingImport inParsingImport) {
        try {
            JobExecution jobExecution;
            final var defaultJobParameters = defaultJobParameters(inParsingImport.getImportId(), inParsingImport.getPluginCode());
            switch (importsConfigProvider.getParsingStrategy()) {
                case LOCAL -> {
                    jobExecution = jobLauncher.run(localParsingJob, defaultJobParameters);
                    log.info("LocalParsingJob launched with importId={} and content={}.", inParsingImport.getImportId(), jobExecution);
                }
                case LOCAL_PARALLEL -> {
                    jobExecution = jobLauncher.run(localParallelParsingJob, defaultJobParameters);
                    log.info("LocalParallelParsingJob launched with importId={} and content={}.", inParsingImport.getImportId(), jobExecution);
                }
                case DISTRIBUTED_PARALLEL -> {
                    jobExecution = jobLauncher.run(distributedParallelParsingJob, defaultJobParameters);
                    log.info("DistributedParallelParsingJob launched with importId={} and content={}.", inParsingImport.getImportId(), jobExecution);
                }
                case DISTRIBUTED -> {
                    jobExecution = jobLauncher.run(distributedParsingJob, defaultJobParameters);
                    log.info("DistributedParsingJob launched with importId={} and content={}.", inParsingImport.getImportId(), jobExecution);
                }
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
