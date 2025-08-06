package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.batch.integration.chunk.ChunkResponse;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
public class ParseChunkMessageResponse extends Message {
    private final ChunkResponse response;
}
