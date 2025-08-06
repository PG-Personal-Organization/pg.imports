package pg.imports.plugin.api.strategies.db;

import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;

/**
 * Used with {@link RecordsStoringStrategy#LIBRARY_JSON_DATABASE}
 * */
public interface LibraryDbStoredRecordsPlugin<T extends RecordData> extends ImportPlugin<T> {
    Class<T> getRecordClass();
}
