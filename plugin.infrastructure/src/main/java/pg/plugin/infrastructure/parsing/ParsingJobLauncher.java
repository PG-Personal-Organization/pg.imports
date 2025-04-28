package pg.plugin.infrastructure.parsing;

import pg.plugin.api.ImportPlugin;
import pg.plugin.infrastructure.states.InParsingImport;

public interface ParsingJobLauncher {

    void launchParsingJob(ImportPlugin importPlugin, InParsingImport inParsingImport);
}
