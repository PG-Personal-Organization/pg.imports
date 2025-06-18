package pg.plugin.api.strategies.self;

import lombok.NonNull;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.records.writing.PluginRecordsWriter;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.api.strategies.db.RecordData;

/**
 * Used with {@link RecordsStoringStrategy#PLUGIN_DATABASE}
 * */
public interface SelfStoringRecordsPlugin<T extends RecordData> extends ImportPlugin<T> {
    @NonNull
    PluginRecordsWriter<T> getRecordsWriter();

    @NonNull
    Class<T> getRecordClass();
}
