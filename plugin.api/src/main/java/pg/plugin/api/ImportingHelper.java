package pg.plugin.api;

import pg.plugin.api.strategies.db.RecordData;

import java.util.Collection;
import java.util.UUID;

public interface ImportingHelper {
    String startImport(ImportPlugin plugin, UUID fileId);

    <T extends RecordData> void save(Collection<ImportedRecord<T>> records, ImportPlugin plugin);
}
