package pg.plugin.infrastructure.spring.batch.importing.distributed.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import pg.kafka.sender.EventSender;

@Log4j2
@RequiredArgsConstructor
@StepScope
public class DistributedImportPartitionResponseSender implements GenericHandler<ImportPartitionMessageResponse> {
    private final EventSender eventSender;

    @Override
    public Object handle(final ImportPartitionMessageResponse request, final MessageHeaders headers) {
        log.info("Sending partition response: {}", request);
        try {
            eventSender.sendEvent(request);
        } catch (final Exception e) {
            log.error("Error during partition sending", e);
            throw new RuntimeException(e);
        }
        return null;
    }
}
