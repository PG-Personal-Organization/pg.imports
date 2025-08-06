package pg.imports.plugin.infrastructure.spring.batch.common.distributed;

public class ChunkHandlingException extends RuntimeException {
    public ChunkHandlingException(final Throwable cause) {
        super(cause);
    }
}