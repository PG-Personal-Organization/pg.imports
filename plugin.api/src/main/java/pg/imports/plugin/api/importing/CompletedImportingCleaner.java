package pg.imports.plugin.api.importing;

import lombok.NonNull;

import java.util.List;

public interface CompletedImportingCleaner {
    void handleCleaningSuccessfulRecords(@NonNull List<String> recordIds);

    void handleCleaningFailedRecords(@NonNull List<String> errorRecordIds);
}
