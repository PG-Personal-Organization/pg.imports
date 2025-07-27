package pg.plugin.api.importing;

import lombok.NonNull;
import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.strategies.db.RecordData;

import java.util.Collections;
import java.util.List;

public interface ImportingComponentsProvider<RECORD extends RecordData, IN extends ParsedRecord<RecordData>, IN_PROVIDER extends ImportingRecordsProvider<IN>> {

    @NonNull
    default ImportingRecordsProvider<IN> getImportingRecordsProvider(List<String> recordIds) {
        return records -> Collections.emptyList();
    }

    @NonNull
    RecordImporter<RECORD, IN, IN_PROVIDER> getRecordImporter();

    @NonNull
    default RecordsImportingErrorHandler getRecordsImportingErrorHandler() {
        return recordIds -> { };
    }

    @NonNull
    default CompletedImportingCleaner getCompletedImportingCleaner() {
        return (recordIds, errorRecordIds) -> { };
    }
}
