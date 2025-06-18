package pg.plugin.infrastructure.spring.batch.parsing.distributed;

public class ChunkSendingException extends RuntimeException {
    public ChunkSendingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}