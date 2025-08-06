package pg.imports.tests.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.importing.ImportingComponentsProvider;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ParsingComponentsProvider;
import pg.imports.plugin.api.strategies.db.RecordData;

@RequiredArgsConstructor
public class TestPlugin implements ImportPlugin<TestRecord> {

    private final ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> parsingProvider;
    private final ImportingComponentsProvider<TestRecord, ParsedRecord<RecordData>, ImportingRecordsProvider<ParsedRecord<RecordData>>> importingProvider;

    @NonNull
    @Override
    public PluginCode getCode() {
        return new PluginCode("TEST");
    }

    @NonNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @NonNull
    @Override
    public String getCodeIdPrefix() {
        return "TEST";
    }

    @Override
    public int getChunkSize() {
        return 10;
    }

    @NonNull
    @Override
    public ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> getParsingComponentProvider() {
        return parsingProvider;
    }

    @NonNull
    @Override
    public ImportingComponentsProvider<TestRecord, ParsedRecord<RecordData>, ImportingRecordsProvider<ParsedRecord<RecordData>>> getImportingComponentsProvider() {
        return importingProvider;
    }

    @Override
    public Class<? extends RecordData> getRecordClass() {
        return TestRecord.class;
    }
}
