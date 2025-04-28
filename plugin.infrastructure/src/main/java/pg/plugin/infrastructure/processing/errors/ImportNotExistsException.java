package pg.plugin.infrastructure.processing.errors;

public class ImportNotExistsException extends RuntimeException {
    public ImportNotExistsException(final String message) {
        super(message);
    }
}
