package pg.plugin.api.importing;

import lombok.NonNull;

import java.util.List;

public interface RecordsImportingErrorHandler {
    void handleError(@NonNull List<String> recordIds);
}
