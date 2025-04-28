package pg.plugin.api;

import pg.plugin.api.data.ImportedRecord;
import pg.plugin.api.data.PluginCode;
import pg.plugin.api.errors.NotImplementedException;
import pg.plugin.api.parsing.ParsingComponentsProvider;
import pg.plugin.api.strategies.db.RecordData;

public interface ImportPlugin<RD extends RecordData> {
    Integer BASIC_CHUNK = 200;

    PluginCode getCode();

    String getVersion();

    String getCodeIdPrefix();

    default int getChunkSize() {
        return BASIC_CHUNK;
    }

    default int getChunkSizeMultiplierForPartitionSize() {
        return 1;
    }

    default <IN, OUT extends ImportedRecord<RD>> ParsingComponentsProvider<RD, IN, OUT> getParsingComponentProvider() {
        throw new NotImplementedException("Parsing implemented for plugin: " + getCode());
    }

    Class<? extends RecordData> getRecordClass();
}
