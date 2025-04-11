package pg.plugin.api.strategies.db;

import pg.plugin.api.ImportPlugin;

/**
 * Used with {@link pg.plugin.api.strategies.RecordsStoring#LIBRARY_JSON_DATABASE}
 * */
public interface LibraryDbStoredRecordsPlugin<T extends RecordData> extends ImportPlugin {
    Class<T> getRecordClass();
}
