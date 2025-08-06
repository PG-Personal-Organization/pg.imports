package pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageRequest;
import pg.imports.plugin.infrastructure.spring.batch.importing.distributed.partition.ImportPartitionMessageResponse;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportPartitionRequestMessageHandler implements MessageHandler<ImportPartitionMessageRequest> {
    private final EventSender eventSender;
    private final StepExecutionRequestHandler stepExecutionRequestHandler;

    @Override
    public void handleMessage(final @NonNull ImportPartitionMessageRequest message) {
        var request = message.getRequest();
        var stepExecution = stepExecutionRequestHandler.handle(request);
        var response = new ImportPartitionMessageResponse(stepExecution);
        try {
            eventSender.sendEvent(response);
        } catch (final Exception e) {
            log.error("Error during partition sending", e);
            throw new RuntimeException(e);
        }
        log.info("Sending partition request response: {}", response);
    }

    @Override
    public Class<ImportPartitionMessageRequest> getMessageType() {
        return ImportPartitionMessageRequest.class;
    }
}
