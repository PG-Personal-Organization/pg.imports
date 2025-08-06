package pg.imports.plugin.infrastructure.parsing;

import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.infrastructure.states.OngoingParsingImport;

public interface ParsingJobLauncher {

    void launchParsingJob(ImportPlugin importPlugin, OngoingParsingImport ongoingParsingImport);
}
