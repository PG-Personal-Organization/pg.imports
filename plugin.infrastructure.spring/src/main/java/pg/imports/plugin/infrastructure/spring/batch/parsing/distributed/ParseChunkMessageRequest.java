package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import pg.imports.plugin.api.data.ImportContext;
import pg.kafka.message.Message;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseChunkMessageRequest extends Message {
    private long jobId;
    private int sequence;
    private List<JsonNode> items;
    private ImportContext importContext;
}
