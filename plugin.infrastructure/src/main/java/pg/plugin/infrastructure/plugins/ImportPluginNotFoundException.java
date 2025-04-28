package pg.plugin.infrastructure.plugins;

public class ImportPluginNotFoundException extends RuntimeException {
    public ImportPluginNotFoundException(final String message) {
        super(message);
    }
}
