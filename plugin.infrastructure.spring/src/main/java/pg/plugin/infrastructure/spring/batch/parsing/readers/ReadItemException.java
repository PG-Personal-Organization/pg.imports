package pg.plugin.infrastructure.spring.batch.parsing.readers;

public class ReadItemException extends RuntimeException {
    public ReadItemException(final String message) {
        super(message);
    }
}
