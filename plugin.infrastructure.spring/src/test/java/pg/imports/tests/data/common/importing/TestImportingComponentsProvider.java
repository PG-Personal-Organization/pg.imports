package pg.imports.tests.data.common.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.importing.*;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.tests.data.common.TestRecord;

@RequiredArgsConstructor
public class TestImportingComponentsProvider implements ImportingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> {
    private final TestRecordImporter testRecordImporter;

    @Override
    public @NonNull RecordImporter<TestRecord, ParsedRecord<TestRecord>> getRecordImporter() {
        return testRecordImporter;
    }

}
