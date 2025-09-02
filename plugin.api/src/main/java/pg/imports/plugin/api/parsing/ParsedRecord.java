package pg.imports.plugin.api.parsing;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.io.Serializable;
import java.util.List;

public interface ParsedRecord<T extends RecordData> extends Serializable {
    @NonNull
    T getRecord();

    @NonNull
    String getRecordId();

    @NonNull
    String getImportId();

    @NonNull
    Long getOrdinal();

    @NonNull
    ImportRecordStatus getRecordStatus();

    @NonNull
    List<String> getErrorMessages();
}
