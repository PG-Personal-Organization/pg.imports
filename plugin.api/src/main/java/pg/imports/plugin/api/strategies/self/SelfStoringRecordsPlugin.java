package pg.imports.plugin.api.strategies.self;

import lombok.NonNull;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.writing.PluginRecordsWriter;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.strategies.db.RecordData;

/**
 * Used with {@link RecordsStoringStrategy#PLUGIN_DATABASE}
 * */
public interface SelfStoringRecordsPlugin<T extends RecordData> extends ImportPlugin<T> {
    @NonNull
    PluginRecordsWriter<T> getRecordsWriter();

    @NonNull
    Class<T> getRecordClass();
}
