package pg.imports.plugin.api.parsing;

import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.io.Serializable;
import java.util.List;

public interface ParsedRecord<T extends RecordData> extends Serializable {
    T getRecord();

    String getRecordId();

    String getImportId();

    Long getOrdinal();

    ImportRecordStatus getRecordStatus();

    List<String> getErrorMessages();
}
