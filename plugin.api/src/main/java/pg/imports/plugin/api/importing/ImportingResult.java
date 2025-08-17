package pg.imports.plugin.api.importing;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;

@AllArgsConstructor(staticName = "error")
@NoArgsConstructor(staticName = "success")
public class ImportingResult {
    private String importingErrorCode;

    public Optional<String> getImportingErrorCode() {
        return Optional.ofNullable(importingErrorCode);
    }
}
