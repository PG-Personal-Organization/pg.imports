package pg.plugin.api.data;

import pg.plugin.api.strategies.db.RecordData;

import java.util.List;

public interface ImportedRecord<T extends RecordData> {
    T getRecord();

    String getImportId();

    Long getOrdinal();

    ImportRecordStatus getRecordStatus();

    List<String> getErrorMessages();
}
