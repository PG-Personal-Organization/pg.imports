package pg.plugin.infrastructure.plugins;

public class DuplicateImportPluginException extends RuntimeException {
    public DuplicateImportPluginException(final String message) {
        super(message);
    }
}
