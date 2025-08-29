package pg.imports.plugin.api;

import lombok.NonNull;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.errors.NotImplementedException;
import pg.imports.plugin.api.importing.ImportingComponentsProvider;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ParsingComponentsProvider;
import pg.imports.plugin.api.strategies.db.RecordData;

public interface ImportPlugin<RECORD_DATA extends RecordData> {
    Integer BASIC_CHUNK = 200;

    @NonNull
    PluginCode getCode();

    @NonNull
    String getVersion();

    @NonNull
    String getCodeIdPrefix();

    @NonNull
    default String getRecordsPrefix() {
        return getCodeIdPrefix() + "_";
    }

    default int getChunkSize() {
        return BASIC_CHUNK;
    }

    default int getChunkSizeMultiplierForPartitionSize() {
        return 1;
    }

    @NonNull
    default ParsingComponentsProvider<RECORD_DATA, ? extends ParsedRecord<RECORD_DATA>> getParsingComponentProvider() {
        throw new NotImplementedException("Parsing not implemented for plugin: " + getCode());
    }

    @NonNull
    default ImportingComponentsProvider<RECORD_DATA, ? extends ParsedRecord<RECORD_DATA>> getImportingComponentsProvider() {
        throw new NotImplementedException("Importing not implemented for plugin: " + getCode());
    }

    Class<? extends RecordData> getRecordClass();
}
