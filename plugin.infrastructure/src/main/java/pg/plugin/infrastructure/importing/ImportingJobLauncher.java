package pg.plugin.infrastructure.importing;

import pg.plugin.api.ImportPlugin;
import pg.plugin.infrastructure.states.AfterParsingImport;

public interface ImportingJobLauncher {
    void launchImportingJob(ImportPlugin importPlugin, AfterParsingImport afterParsingImport);
}
