package pg.plugin.api.parsing;

import lombok.NonNull;
import pg.plugin.api.data.ImportedRecord;
import pg.plugin.api.errors.NotImplementedException;
import pg.plugin.api.strategies.db.RecordData;

public interface ParsingComponentsProvider<RECORD_DATA extends RecordData, IN, OUT extends ImportedRecord<RECORD_DATA>> {
    @NonNull
    default ReaderDefinition getReaderDefinition() {
        throw new NotImplementedException("Reader definition not implemented for plugin");
    }

    @NonNull
    default RecordParser<IN, RECORD_DATA, OUT> getRecordParser() {
        throw new NotImplementedException("Not implemented for plugin");
    }

    @NonNull
    default RecordsParsingErrorHandler getRecordsParsingErrorHandler() {
        return (recordIds) -> { };
    }
}
