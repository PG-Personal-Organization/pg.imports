package pg.plugin.api.importing;

import lombok.NonNull;

import java.util.List;

public interface CompletedImportingCleaner {
    void handleCleaning(@NonNull List<String> recordIds, @NonNull List<String> errorRecordIds);
}
