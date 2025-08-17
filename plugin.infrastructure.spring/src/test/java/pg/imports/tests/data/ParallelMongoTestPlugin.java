package pg.imports.tests.data;

import lombok.NonNull;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.importing.ImportingComponentsProvider;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ParsingComponentsProvider;
import pg.imports.tests.data.common.TestRecord;

public class ParallelMongoTestPlugin extends TestPlugin {
    public ParallelMongoTestPlugin(final ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> parsingProvider,
                                   final ImportingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> importingProvider) {
        super(parsingProvider, importingProvider);
    }

    @NonNull
    @Override
    public PluginCode getCode() {
        return new PluginCode("PARALLEL_MONGO");
    }

    @NonNull
    @Override
    public String getCodeIdPrefix() {
        return "PARALLEL_MONGO";
    }

}
