package pg.plugin.infrastructure.parsing;

import pg.plugin.api.ImportPlugin;
import pg.plugin.infrastructure.states.OngoingParsingImport;

public interface ParsingJobLauncher {

    void launchParsingJob(ImportPlugin importPlugin, OngoingParsingImport ongoingParsingImport);
}
