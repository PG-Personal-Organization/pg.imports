package pg.imports.plugin.api.reason;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportRejectionReasons {
    public static final String UNEXPECTED = "UNEXPECTED";
    public static final String NO_RECORDS_FOUND = "NO_RECORDS_FOUND";
    public static final String FAILED_RECORDS_PARSING = "FAILED_RECORDS_PARSING";
}
