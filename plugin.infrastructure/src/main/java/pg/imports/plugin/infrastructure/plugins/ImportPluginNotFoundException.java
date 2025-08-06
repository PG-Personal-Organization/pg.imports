package pg.imports.plugin.infrastructure.plugins;

public class ImportPluginNotFoundException extends RuntimeException {
    public ImportPluginNotFoundException(final String message) {
        super(message);
    }
}
