package pg.plugin.api;

import lombok.NonNull;
import pg.plugin.api.data.ImportedRecord;
import pg.plugin.api.data.PluginCode;
import pg.plugin.api.errors.NotImplementedException;
import pg.plugin.api.parsing.ParsingComponentsProvider;
import pg.plugin.api.strategies.db.RecordData;

public interface ImportPlugin<RECORD_DATA extends RecordData> {
    Integer BASIC_CHUNK = 200;

    @NonNull
    PluginCode getCode();

    @NonNull
    String getVersion();

    @NonNull
    String getCodeIdPrefix();

    default int getChunkSize() {
        return BASIC_CHUNK;
    }

    default int getChunkSizeMultiplierForPartitionSize() {
        return 1;
    }

    @NonNull
    default <IN, OUT extends ImportedRecord<RECORD_DATA>> ParsingComponentsProvider<RECORD_DATA, IN, OUT> getParsingComponentProvider() {
        throw new NotImplementedException("Parsing implemented for plugin: " + getCode());
    }

    Class<? extends RecordData> getRecordClass();
}
