package pg.plugin.infrastructure.states;

import pg.plugin.api.data.ImportId;
import pg.plugin.api.data.PluginCode;

import java.util.UUID;

public interface Import {
    ImportId getImportId();

    PluginCode getPluginCode();

    UUID getFileId();
}
