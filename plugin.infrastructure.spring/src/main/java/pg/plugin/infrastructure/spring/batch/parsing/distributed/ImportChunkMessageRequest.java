package pg.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.*;
import org.springframework.batch.integration.chunk.ChunkRequest;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
public class ImportChunkMessageRequest extends Message {
    private final ChunkRequest chunk;
}
