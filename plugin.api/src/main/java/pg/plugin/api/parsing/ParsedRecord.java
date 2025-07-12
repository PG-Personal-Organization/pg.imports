package pg.plugin.api.parsing;

import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.strategies.db.RecordData;

import java.io.Serializable;
import java.util.List;

public interface ParsedRecord<T extends RecordData> extends Serializable {
    T getRecord();

    String getImportId();

    Long getOrdinal();

    ImportRecordStatus getRecordStatus();

    List<String> getErrorMessages();
}
