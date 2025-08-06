package pg.imports.plugin.api.importing;

import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.io.Serializable;
import java.util.List;

public interface ImportedRecord<T extends RecordData> extends Serializable {
    T getRecord();

    String getImportId();

    Long getOrdinal();

    ImportRecordStatus getRecordStatus();

    List<String> getErrorMessages();
}
