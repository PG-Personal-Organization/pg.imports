package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition;

import lombok.*;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportPartitionMessageRequest extends Message {
    private StepExecutionRequest request;
}
