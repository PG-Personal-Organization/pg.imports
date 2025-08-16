package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.kafka.sender.EventSender;

@Log4j2
@RequiredArgsConstructor
public class DistributedParseChunkSender implements MessageHandler {
    private final EventSender eventSender;
    private final ObjectMapper batchObjectMapper;

    @Override
    public void handleMessage(final @NonNull Message<?> message) throws MessagingException {
        Object payload = message.getPayload();

        if (!(payload instanceof ChunkRequest<?> chunkRequest)) {
            log.warn("Ignoring message with unexpected payload: {}", payload.getClass());
            return;
        }

        log.info("Sending chunk request: seq={}, jobId={}, items={}",
                chunkRequest.getSequence(), chunkRequest.getJobId(), chunkRequest.getItems().size());

        try {
            var importContext = JobUtil.getImportContext(chunkRequest.getStepContribution().getStepExecution());
            var items = chunkRequest.getItems().getItems()
                    .stream()
                    .map(item -> (JsonNode) batchObjectMapper.valueToTree(item))
                    .toList();
            var outbound = new ParseChunkMessageRequest(chunkRequest.getJobId(), chunkRequest.getSequence(), items, importContext);
            eventSender.sendEvent(outbound);
        } catch (Exception e) {
            log.error("Error during chunk sending (seq={})", chunkRequest.getSequence(), e);
            throw new MessagingException(message, "Failed to send chunk request", e);
        }
    }
}
