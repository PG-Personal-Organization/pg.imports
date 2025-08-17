package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition;

import lombok.*;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportPartitionMessageResponse extends Message {
    private long jobExecutionId;
    private long stepExecutionId;
}
