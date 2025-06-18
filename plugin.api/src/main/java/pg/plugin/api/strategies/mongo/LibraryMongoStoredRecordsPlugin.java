package pg.plugin.api.strategies.mongo;

import pg.plugin.api.ImportPlugin;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.api.strategies.db.RecordData;

/**
 * Used with {@link RecordsStoringStrategy#MONGO_REPOSITORY}
 */
public interface LibraryMongoStoredRecordsPlugin<T extends RecordData> extends ImportPlugin<T> { }

