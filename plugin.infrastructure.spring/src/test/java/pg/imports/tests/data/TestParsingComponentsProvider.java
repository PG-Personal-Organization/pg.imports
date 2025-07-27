package pg.imports.tests.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.plugin.api.parsing.ReaderDefinition;
import pg.plugin.api.parsing.RecordParser;
import pg.plugin.api.parsing.RecordsParsingErrorHandler;
import pg.plugin.api.parsing.ParsingComponentsProvider;

@RequiredArgsConstructor
public class TestParsingComponentsProvider implements ParsingComponentsProvider<TestRecordData, String, ReadOnlyParsedRecord> {

    private final RecordParser<String, TestRecordData, ReadOnlyParsedRecord> recordParser;
    private final ReaderDefinition readerDefinition;
    private final RecordsParsingErrorHandler errorHandler;

    @NonNull
    @Override
    public ReaderDefinition getReaderDefinition() {
        return readerDefinition;
    }

    @NonNull
    @Override
    public RecordParser<String, TestRecordData, ReadOnlyParsedRecord> getRecordParser() {
        return recordParser;
    }

    @NonNull
    @Override
    public RecordsParsingErrorHandler getRecordsParsingErrorHandler() {
        return errorHandler;
    }
}
