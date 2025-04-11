package pg.plugin.api.strategies.mongo;

import pg.plugin.api.ImportPlugin;

/**
 * Used with {@link pg.plugin.api.strategies.RecordsStoring#MONGO_REPOSITORY}
 * */
public interface LibraryMongoStoredRecordsPlugin<T extends RecordDocument> extends ImportPlugin {
    Class<T> getDocumentClass();
}
