package pg.imports.tests.data.common.parsing;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.imports.plugin.api.parsing.ReaderOutputItem;
import pg.imports.plugin.api.parsing.RecordParser;
import pg.imports.tests.data.common.TestRecord;

import java.util.Collections;

public class TestRecordParser implements RecordParser<TestRecord, ParsedRecord<TestRecord>> {

    @Override
    @NonNull
    public ParsedRecord<TestRecord> parse(final ReaderOutputItem<Object> item, final ImportContext importContext) {
            var importId = importContext.getImportId();
        try {
            TestRecord data = (TestRecord) item.getRawItem();
            return ReadOnlyParsedRecord.<TestRecord>builder()
                    .recordId(item.getId())
                    .importId(importId.id())
                    .recordData(data)
                    .ordinal(item.getItemNumber())
                    .recordStatus(ImportRecordStatus.PARSED)
                    .errorMessages(Collections.emptyList())
                    .build();
        } catch (final Exception e) {
            return ReadOnlyParsedRecord.<TestRecord>builder()
                    .recordId(item.getId())
                    .importId(importId.id())
                    .recordData(null)
                    .ordinal(item.getItemNumber())
                    .recordStatus(ImportRecordStatus.PARSING_FAILED)
                    .errorMessages(Collections.singletonList(e.getMessage()))
                    .build();
        }
    }

}
