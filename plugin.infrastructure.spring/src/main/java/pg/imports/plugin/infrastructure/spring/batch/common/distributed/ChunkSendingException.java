package pg.imports.plugin.infrastructure.spring.batch.common.distributed;

public class ChunkSendingException extends RuntimeException {
    public ChunkSendingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}