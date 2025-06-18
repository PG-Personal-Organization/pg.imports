package pg.plugin.api.parsing;

import lombok.NonNull;

import java.util.List;

public interface RecordsParsingErrorHandler {
    void handleError(@NonNull List<String> recordIds);
}
