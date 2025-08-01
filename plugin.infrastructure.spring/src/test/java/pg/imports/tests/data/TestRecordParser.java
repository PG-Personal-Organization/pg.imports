package pg.imports.tests.data;

import lombok.NonNull;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.api.parsing.RecordParser;

import java.util.Collections;

public class TestRecordParser implements RecordParser<TestRecordData, ReadOnlyParsedRecord<TestRecordData>> {

    @Override
    @NonNull
    public ReadOnlyParsedRecord<TestRecordData> parse(final ReaderOutputItem<Object> item, final ImportContext importContext) {
            var importId = importContext.getImportId();
        try {
            TestRecordData data = (TestRecordData) item.getRawItem();
            return ReadOnlyParsedRecord.<TestRecordData>builder()
                    .importId(importId.id())
                    .recordData(data)
                    .ordinal(item.getItemNumber())
                    .recordStatus(ImportRecordStatus.PARSED)
                    .errorMessages(Collections.emptyList())
                    .build();
        } catch (final Exception e) {
            return ReadOnlyParsedRecord.<TestRecordData>builder()
                    .importId(importId.id())
                    .recordData(null)
                    .ordinal(item.getItemNumber())
                    .recordStatus(ImportRecordStatus.PARSING_FAILED)
                    .errorMessages(Collections.singletonList(e.getMessage()))
                    .build();
        }
    }

}
