package pg.imports.tests.data;

import lombok.NonNull;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.api.parsing.RecordParser;

import java.util.Collections;

public class TestRecordParser implements RecordParser<String, TestRecordData, ReadOnlyParsedRecord> {

    @Override
    public ReadOnlyParsedRecord parse(ReaderOutputItem<String> item) {
        try {
            String[] parts = data.split(",");
            TestRecordData recordData = TestRecordData.builder()
                    .name(parts[0])
                    .value(parts[1])
                    .orderId(Integer.parseInt(parts[2]))
                    .build();

            return ReadOnlyParsedRecord.builder()
                    .importId(importId)
                    .recordData(recordData)
                    .ordinal(item.getItemNumber())
                    .recordStatus(ImportRecordStatus.PARSED)
                    .errorMessages(Collections.emptyList())
                    .build();
        } catch (Exception e) {
            return ReadOnlyParsedRecord.builder()
                    .importId(importId)
                    .recordData(null)
                    .ordinal((int) ordinal)
                    .recordStatus(ImportRecordStatus.PARSING_FAILED)
                    .errorMessages(Collections.singletonList(e.getMessage()))
                    .build();
        }
    }

}
