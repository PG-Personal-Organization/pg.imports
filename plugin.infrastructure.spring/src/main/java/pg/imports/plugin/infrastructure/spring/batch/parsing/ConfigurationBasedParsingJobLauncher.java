package pg.imports.plugin.infrastructure.spring.batch.parsing;

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
import pg.imports.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportStatus;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.LocalJobRegistry;
import pg.imports.plugin.infrastructure.states.OngoingParsingImport;

@Log4j2
@RequiredArgsConstructor
public class ConfigurationBasedParsingJobLauncher implements ParsingJobLauncher {
    private final ImportRepository importRepository;
    private final ImportsConfigProvider importsConfigProvider;
    private final JobLauncher jobLauncher;
    private final LocalJobRegistry localJobRegistry;

    private final Job localParsingJob;
    private final Job localParallelParsingJob;
    private final Job distributedParsingJob;

    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void launchParsingJob(final ImportPlugin importPlugin, final OngoingParsingImport ongoingParsingImport) {
        try {
            JobExecution jobExecution;
            ImportStatus importStatus = importRepository.findById(ongoingParsingImport.getImportId().id()).map(ImportEntity::getStatus).orElse(null);
            if (importStatus == null || !importStatus.equals(ImportStatus.ONGOING_PARSING)) {
                log.warn("Parsing job cannot be launched because import status is not ONGOING_PARSING. ImportId: {}, status: {}", ongoingParsingImport.getImportId(), importStatus);
                return;
            }

            final var pluginCode = ongoingParsingImport.getPluginCode();
            final var defaultJobParameters = defaultJobParameters(ongoingParsingImport.getImportId(), pluginCode);
            switch (importsConfigProvider.getParsingStrategy(pluginCode)) {
                case LOCAL -> {
                    log.info("LocalParsingJob started with importId={} and content={}.", ongoingParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(localParsingJob, defaultJobParameters);
                }
                case LOCAL_PARALLEL -> {
                    log.info("LocalParallelParsingJob started with importId={} and content={}.", ongoingParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(localParallelParsingJob, defaultJobParameters);
                }
                case DISTRIBUTED -> {
                    log.info("DistributedParsingJob started with importId={} and content={}.", ongoingParsingImport.getImportId(), defaultJobParameters);
                    jobExecution = jobLauncher.run(distributedParsingJob, defaultJobParameters);
                    localJobRegistry.register(jobExecution.getId());
                }
                default -> throw new IllegalArgumentException("Unknown parsing strategy: " + importsConfigProvider.getParsingStrategy(pluginCode));
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
