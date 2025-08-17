package pg.imports.plugin.api.importing;

import lombok.NonNull;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.Collections;
import java.util.List;

public interface ImportingComponentsProvider<RECORD extends RecordData, IN extends ParsedRecord<RECORD>> {

    /**
     * Used only for {@link pg.imports.plugin.api.strategies.RecordsStoringStrategy.PLUGIN_DATABASE}
     * */
    @NonNull
    default ImportingRecordsProvider<IN> getPluginImportingRecordsProvider(List<String> successfulRecordIds) {
        return Collections::emptyList;
    }

    @NonNull
    RecordImporter<RECORD, IN> getRecordImporter();

    @NonNull
    default RecordsImportingErrorHandler getRecordsImportingErrorHandler() {
        return recordIds -> { };
    }

    @NonNull
    default CompletedImportingCleaner getCompletedImportingCleaner() {
        return new CompletedImportingCleaner() {
            @Override
            public void handleCleaningSuccessfulRecords(final @NonNull List<String> recordIds) { }

            @Override
            public void handleCleaningFailedRecords(final @NonNull List<String> errorRecordIds) { }
        };
    }
}
