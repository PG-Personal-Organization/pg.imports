package pg.imports.plugin.infrastructure.importing;

import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.infrastructure.states.OngoingImportingImport;

public interface ImportingJobLauncher {
    void launchImportingJob(ImportPlugin importPlugin, OngoingImportingImport afterParsingImport);
}
