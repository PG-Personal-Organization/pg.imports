package pg.imports.plugin.api.parsing;

import lombok.NonNull;
import pg.imports.plugin.api.errors.NotImplementedException;
import pg.imports.plugin.api.strategies.db.RecordData;

public interface ParsingComponentsProvider<RECORD_DATA extends RecordData, OUT extends ParsedRecord<RECORD_DATA>> {
    @NonNull
    default ReaderDefinition getReaderDefinition() {
        throw new NotImplementedException("Reader definition not implemented for plugin");
    }

    @NonNull
    default RecordParser<RECORD_DATA, OUT> getRecordParser() {
        throw new NotImplementedException("Not implemented for plugin");
    }

    @NonNull
    default RecordsParsingErrorHandler getRecordsParsingErrorHandler() {
        return (recordIds) -> { };
    }
}
