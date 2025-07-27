package pg.plugin.infrastructure.importing;

import pg.plugin.api.ImportPlugin;
import pg.plugin.infrastructure.states.OngoingImportingImport;

public interface ImportingJobLauncher {
    void launchImportingJob(ImportPlugin importPlugin, OngoingImportingImport afterParsingImport);
}
