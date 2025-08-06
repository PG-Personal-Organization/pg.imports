package pg.imports.plugin.infrastructure.states;

import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.PluginCode;

import java.util.UUID;

public interface Import {
    ImportId getImportId();

    PluginCode getPluginCode();

    UUID getFileId();
}
