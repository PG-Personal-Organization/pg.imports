package pg.imports.plugin.infrastructure.processing.errors;

public class ImportFileNotFoundException extends RuntimeException {
    public ImportFileNotFoundException(final String message) {
        super(message);
    }
}
