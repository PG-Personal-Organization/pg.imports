package pg.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.integration.chunk.ChunkResponse;
import org.springframework.batch.item.Chunk;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.spring.batch.common.distributed.ChunkHandlingException;
import pg.plugin.infrastructure.spring.batch.parsing.processor.DistributedOutputItemProcessor;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;
import pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager;

import java.util.ArrayList;

@Log4j2
@RequiredArgsConstructor
public class DistributedParseChunkRequestHandler implements MessageHandler<ParseChunkMessageRequest> {
    private final RecordsWriterManager recordsWriterManager;
    private final DistributedOutputItemProcessor itemProcessor;
    private final EventSender eventSender;

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(final @NonNull ParseChunkMessageRequest message) {
        var chunkRequest = message.getChunk();

        try {
            Chunk<ReaderOutputItem<Object>> inputItem = chunkRequest.getItems();
            var processedItems = new ArrayList<PartitionedRecord>();

            for (var item : inputItem.getItems()) {
                processedItems.add(itemProcessor.process(item));
            }

            log.debug("Chunk {} processed items: {}", chunkRequest.getSequence(), processedItems);
            recordsWriterManager.write(new Chunk<>(processedItems));

            var response = new ChunkResponse(true, chunkRequest.getSequence(), chunkRequest.getJobId(), null);
            eventSender.sendEvent(new ParseChunkMessageResponse(response));
        } catch (final Exception e) {
            log.error("Error during chunk handling", e);
            var response = new ChunkResponse(false, chunkRequest.getSequence(), chunkRequest.getJobId(), null);
            eventSender.sendEvent(new ParseChunkMessageResponse(response));
            throw new ChunkHandlingException(e);
        }
    }

    @Override
    public Class<ParseChunkMessageRequest> getMessageType() {
        return ParseChunkMessageRequest.class;
    }
}
