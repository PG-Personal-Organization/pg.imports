package pg.imports.tests.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.PluginCode;
import pg.plugin.api.importing.ImportingComponentsProvider;
import pg.plugin.api.importing.ImportingRecordsProvider;
import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.parsing.ParsingComponentsProvider;
import pg.plugin.api.strategies.db.RecordData;

@RequiredArgsConstructor
public class TestPlugin implements ImportPlugin<TestRecordData> {

    private final ParsingComponentsProvider<TestRecordData, String, ParsedRecord<TestRecordData>> parsingProvider;
    private final ImportingComponentsProvider<TestRecordData, ParsedRecord<RecordData>, ImportingRecordsProvider<ParsedRecord<RecordData>>> importingProvider;

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
    public <IN, OUT extends ParsedRecord<TestRecordData>> ParsingComponentsProvider<TestRecordData, IN, OUT> getParsingComponentProvider() {
        return (ParsingComponentsProvider<TestRecordData, IN, OUT>) parsingProvider;
    }

    @NonNull
    @Override
    public <RECORD extends RecordData, IN extends ParsedRecord<RecordData>, IN_PROVIDER extends ImportingRecordsProvider<IN>>
    ImportingComponentsProvider<RECORD, IN, IN_PROVIDER> getImportingComponentsProvider() {
        return (ImportingComponentsProvider<RECORD, IN, IN_PROVIDER>) importingProvider;
    }

    @Override
    public Class<? extends RecordData> getRecordClass() {
        return TestRecordData.class;
    }
}
