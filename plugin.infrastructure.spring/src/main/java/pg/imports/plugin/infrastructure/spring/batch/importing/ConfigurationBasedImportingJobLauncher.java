package pg.imports.plugin.infrastructure.spring.batch.importing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportStatus;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.LocalJobRegistry;
import pg.imports.plugin.infrastructure.states.OngoingImportingImport;

@Log4j2
@RequiredArgsConstructor
public class ConfigurationBasedImportingJobLauncher implements ImportingJobLauncher {
    private final ImportRepository importRepository;
    private final ImportsConfigProvider importsConfigProvider;
    private final JobLauncher jobLauncher;
    private final LocalJobRegistry localJobRegistry;

    private final Job localImportingJob;
    private final Job localParallelImportingJob;
    private final Job distributedImportingJob;

    @Override
    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void launchImportingJob(final ImportPlugin importPlugin, final OngoingImportingImport afterParsingImport) {
        try {
            JobExecution jobExecution;
            ImportStatus importStatus = importRepository.findById(afterParsingImport.getImportId().id()).map(ImportEntity::getStatus).orElse(null);
            if (importStatus == null || !importStatus.equals(ImportStatus.ONGOING_IMPORTING)) {
                log.warn("Importing job cannot be launched because import status is not ONGOING_IMPORTING. ImportId: {}, status: {}", afterParsingImport.getImportId(),
                        importStatus);
                return;
            }

            final var pluginCode = afterParsingImport.getPluginCode();
            final var defaultJobParameters = defaultJobParameters(afterParsingImport.getImportId(), pluginCode);
            switch (importsConfigProvider.getImportingStrategy(pluginCode)) {
                case LOCAL -> {
                    log.info("Starting LocalImportingJob with importId={} and content={}.", afterParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(localImportingJob, defaultJobParameters);
                    log.info("LocalImportingJob finished with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                case LOCAL_PARALLEL -> {
                    log.info("Starting LocalParallelImportingJob with importId={} and content={}.", afterParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(localParallelImportingJob, defaultJobParameters);
                    log.info("LocalParallelImportingJob finished with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                case DISTRIBUTED -> {
                    log.info("Starting DistributedImportingJob with importId={} and content={}.", afterParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(distributedImportingJob, defaultJobParameters);
                    localJobRegistry.register(jobExecution.getId());
                    log.info("DistributedImportingJob finished with importId={} and content={}.", afterParsingImport.getImportId(), jobExecution);
                }
                default -> throw new IllegalArgumentException("Unknown importing strategy: " + importsConfigProvider.getImportingStrategy(pluginCode));
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
