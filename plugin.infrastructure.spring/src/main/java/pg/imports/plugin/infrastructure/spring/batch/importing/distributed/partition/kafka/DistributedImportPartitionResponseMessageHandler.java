package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.DistributedResponseConsumerGroupProvider;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;
import pg.kafka.consumer.MessageHandler;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportPartitionResponseMessageHandler implements MessageHandler<ImportPartitionMessageResponse> {
    private final MessageChannel importingRepliesBus;
    private final DistributedResponseConsumerGroupProvider distributedResponseConsumerGroupProvider;

    @Override
    public void handleMessage(final @NonNull ImportPartitionMessageResponse message) {
        var out = org.springframework.messaging.support.MessageBuilder
                .withPayload(message)
                .setHeader("jobExecutionId", message.getJobExecutionId())
                .setHeader("stepExecutionId", message.getStepExecutionId())
                .build();
        importingRepliesBus.send(new GenericMessage<>(out));
    }

    @Override
    public Class<ImportPartitionMessageResponse> getMessageType() {
        return ImportPartitionMessageResponse.class;
    }

    @Override
    public Optional<String> getConsumerGroup() {
        return Optional.of(distributedResponseConsumerGroupProvider.getConsumerGroup("chunk-response-importing-batch-topic"));
    }
}
