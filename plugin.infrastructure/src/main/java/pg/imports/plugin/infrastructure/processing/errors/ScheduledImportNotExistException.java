package pg.imports.plugin.infrastructure.processing.errors;

public class ScheduledImportNotExistException extends RuntimeException {
    public ScheduledImportNotExistException(final String message) {
        super(message);
    }
}
