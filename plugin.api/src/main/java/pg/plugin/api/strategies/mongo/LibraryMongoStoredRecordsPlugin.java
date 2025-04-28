package pg.plugin.api.strategies.mongo;

import pg.plugin.api.ImportPlugin;
import pg.plugin.api.strategies.RecordsStoringStrategy;

/**
 * Used with {@link RecordsStoringStrategy#MONGO_REPOSITORY}
 * */
public interface LibraryMongoStoredRecordsPlugin<T extends RecordDocument> extends ImportPlugin {
    Class<T> getDocumentClass();
}
