package pg.plugin.api.service;

import pg.plugin.api.data.ImportId;

import java.util.UUID;

public interface ImportingHelper {
    ImportId scheduleImport(String pluginCode, UUID fileId);

//    <T extends RecordData> void save(Collection<ImportedRecord<T>> records, ImportPlugin plugin);
}
