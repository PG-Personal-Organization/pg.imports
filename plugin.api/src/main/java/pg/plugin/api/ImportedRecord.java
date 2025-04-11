package pg.plugin.api;

import pg.plugin.api.strategies.db.RecordData;

public interface ImportedRecord<T extends RecordData> {
    T getRecord();

    String getImportId();

    Long getOrdinal();

    ImportRecordStatus getRecordStatus();
}
