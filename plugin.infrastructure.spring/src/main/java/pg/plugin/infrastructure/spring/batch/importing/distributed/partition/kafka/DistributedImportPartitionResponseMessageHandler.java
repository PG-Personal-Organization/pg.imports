package pg.plugin.infrastructure.spring.batch.importing.distributed.partition.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import pg.kafka.consumer.MessageHandler;
import pg.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportPartitionResponseMessageHandler implements MessageHandler<ImportPartitionMessageResponse> {
    private final PollableChannel importingReplies;

    @Override
    public void handleMessage(final @NonNull ImportPartitionMessageResponse message) {
        StepExecution stepExecution = message.getStepExecution();
        importingReplies.send(new GenericMessage<>(stepExecution));
    }

    @Override
    public Class<ImportPartitionMessageResponse> getMessageType() {
        return ImportPartitionMessageResponse.class;
    }
}
