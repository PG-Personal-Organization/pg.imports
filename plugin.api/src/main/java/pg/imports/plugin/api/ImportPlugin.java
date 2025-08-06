package pg.imports.plugin.api;

import lombok.NonNull;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.errors.NotImplementedException;
import pg.imports.plugin.api.importing.ImportingComponentsProvider;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
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

    default int getChunkSize() {
        return BASIC_CHUNK;
    }

    default int getChunkSizeMultiplierForPartitionSize() {
        return 1;
    }

    @NonNull
    default <OUT extends ParsedRecord<RECORD_DATA>> ParsingComponentsProvider<RECORD_DATA, OUT> getParsingComponentProvider() {
        throw new NotImplementedException("Parsing not implemented for plugin: " + getCode());
    }

    @NonNull
    default <RECORD extends RecordData, IN extends ParsedRecord<RecordData>, IN_PROVIDER extends ImportingRecordsProvider<IN>>
    ImportingComponentsProvider<RECORD, IN, IN_PROVIDER> getImportingComponentsProvider() {
        throw new NotImplementedException("Importing not implemented for plugin: " + getCode());
    }

    Class<? extends RecordData> getRecordClass();
}
