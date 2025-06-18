package pg.plugin.api.strategies.db;

import pg.plugin.api.ImportPlugin;
import pg.plugin.api.strategies.RecordsStoringStrategy;

/**
 * Used with {@link RecordsStoringStrategy#LIBRARY_JSON_DATABASE}
 * */
public interface LibraryDbStoredRecordsPlugin<T extends RecordData> extends ImportPlugin<T> {
    Class<T> getRecordClass();
}
