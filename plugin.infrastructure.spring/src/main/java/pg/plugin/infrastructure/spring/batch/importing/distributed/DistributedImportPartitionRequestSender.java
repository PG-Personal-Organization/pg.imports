package pg.plugin.infrastructure.spring.batch.importing.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import pg.kafka.sender.EventSender;

@Log4j2
@RequiredArgsConstructor
@StepScope
public class DistributedImportPartitionRequestSender implements GenericHandler<StepExecutionRequest> {
    private final EventSender eventSender;

    @Override
    public Object handle(final StepExecutionRequest request, final MessageHeaders headers) {
        log.info("Sending partition request: sequence={}, jobId={}, step={}",
                request.getStepExecutionId(),
                request.getJobExecutionId(),
                request.getStepName()
        );
        var importPartitionMessageRequest = new ImportPartitionMessageRequest(request);
        try {
            eventSender.sendEvent(importPartitionMessageRequest);
        } catch (final Exception e) {
            log.error("Error during partition sending", e);
            throw new RuntimeException(e);
        }
        return null;
    }

}
