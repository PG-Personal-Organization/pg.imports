package pg.plugin.infrastructure.spring.batch.importing.distributed.partition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import pg.kafka.message.Message;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
@AllArgsConstructor
public class ImportPartitionMessageRequest extends Message {
    private final StepExecutionRequest request;
}
