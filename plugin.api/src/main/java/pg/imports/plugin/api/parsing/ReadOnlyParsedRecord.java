package pg.imports.plugin.api.parsing;

import lombok.*;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.List;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString(callSuper = true)
public class ReadOnlyParsedRecord<R extends RecordData> implements ParsedRecord<R> {
    private String importId;
    private String id;
    private R recordData;
    private int ordinal;
    private ImportRecordStatus recordStatus;
    private List<String> errorMessages;

    @Override
    public R getRecord() {
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
