package pg.imports.tests.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.importing.ImportingComponentsProvider;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ParsingComponentsProvider;
import pg.imports.plugin.api.strategies.db.RecordData;
import pg.imports.tests.data.common.TestRecord;

@RequiredArgsConstructor
public abstract class TestPlugin implements ImportPlugin<TestRecord> {

    private final ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> parsingProvider;
    private final ImportingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> importingProvider;

    @NonNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public int getChunkSize() {
        return 200;
    }

    @NonNull
    @Override
    public ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> getParsingComponentProvider() {
        return parsingProvider;
    }

    @NonNull
    @Override
    public ImportingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> getImportingComponentsProvider() {
        return importingProvider;
    }

    @Override
    public Class<? extends RecordData> getRecordClass() {
        return TestRecord.class;
    }
}
