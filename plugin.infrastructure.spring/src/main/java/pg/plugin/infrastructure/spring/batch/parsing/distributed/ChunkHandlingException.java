package pg.plugin.infrastructure.spring.batch.parsing.distributed;

public class ChunkHandlingException extends RuntimeException {
    public ChunkHandlingException(final Throwable cause) {
        super(cause);
    }
}