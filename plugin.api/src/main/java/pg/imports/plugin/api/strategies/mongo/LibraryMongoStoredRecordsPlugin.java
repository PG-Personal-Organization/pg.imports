package pg.imports.plugin.api.strategies.mongo;

import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.strategies.db.RecordData;

/**
 * Used with {@link RecordsStoringStrategy#MONGO_REPOSITORY}
 */
public interface LibraryMongoStoredRecordsPlugin<T extends RecordData> extends ImportPlugin<T> { }

