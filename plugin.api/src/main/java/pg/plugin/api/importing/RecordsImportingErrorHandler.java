package pg.plugin.api.importing;

import lombok.NonNull;

import java.util.List;

public interface RecordsImportingErrorHandler {
    void handleImportingError(@NonNull List<String> allRecordIds);
}
