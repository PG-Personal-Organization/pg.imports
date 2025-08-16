package pg.imports.plugin.api.service;

import pg.imports.plugin.api.data.ImportId;

import java.util.UUID;

public interface ImportingHelper {
    ImportId scheduleImport(String pluginCode, UUID fileId);

    void confirmImporting(ImportId importId);
}
