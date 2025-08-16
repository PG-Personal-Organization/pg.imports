package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.*;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseChunkMessageResponse extends Message {
    private long jobId;
    private int sequence;
    private boolean success;
    private String error;
}
