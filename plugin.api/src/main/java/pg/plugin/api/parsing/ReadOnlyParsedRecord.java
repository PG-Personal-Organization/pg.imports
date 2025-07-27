package pg.plugin.api.parsing;

import lombok.*;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.strategies.db.RecordData;

import java.util.List;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString(callSuper = true)
public class ReadOnlyParsedRecord implements ParsedRecord<RecordData> {
    private String importId;
    private String id;
    private RecordData recordData;
    private int ordinal;
    private ImportRecordStatus recordStatus;
    private List<String> errorMessages;

    @Override
    public RecordData getRecord() {
        return recordData;
    }

    @Override
    public String getImportId() {
        return importId;
    }

    @Override
    public Long getOrdinal() {
        return (long) ordinal;
    }

    @Override
    public ImportRecordStatus getRecordStatus() {
        return recordStatus;
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
