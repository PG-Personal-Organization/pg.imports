package pg.imports.plugin.api.importing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(staticName = "success")
public final class ImportingResult {
    private String importingErrorCode;

    private Map</* recordId */String, String> errorMessages;

    @SuppressWarnings("checkstyle:HiddenField")
    private ImportingResult(final String importingErrorCode) {
        this.importingErrorCode = importingErrorCode;
    }

    public static ImportingResult error(final @NonNull String importingErrorCode) {
        return new ImportingResult(importingErrorCode);
    }

    public static ImportingResult error(final @NonNull String importingErrorCode, final @NonNull Map<String, String> errorMessages) {
        return new ImportingResult(importingErrorCode, errorMessages);
    }

    public Optional<String> getImportingErrorCode() {
        return Optional.ofNullable(importingErrorCode);
    }

    public Optional<Map<String, String>> getErrorMessages() {
        return Optional.ofNullable(errorMessages);
    }
}
