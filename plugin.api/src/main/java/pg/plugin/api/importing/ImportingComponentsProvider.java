package pg.plugin.api.importing;

import lombok.NonNull;
import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.strategies.db.RecordData;

public interface ImportingComponentsProvider<RECORD extends RecordData, IN extends ParsedRecord<RecordData>, IN_PROVIDER extends ImportingRecordsProvider<IN>> {

    @NonNull
    RecordImporter<RECORD, IN, IN_PROVIDER> getRecordImporter();

    @NonNull
    default RecordsImportingErrorHandler getRecordsImportingErrorHandler() {
        return (recordIds) -> { };
    }
}
