package pg.plugin.infrastructure.spring.batch.importing.distributed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.integration.chunk.ChunkResponse;
import org.springframework.batch.item.Chunk;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.spring.batch.common.distributed.ChunkHandlingException;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportChunkRequestHandler implements MessageHandler<ImportChunkMessageRequest> {
    private final EventSender eventSender;

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(final @NonNull ImportChunkMessageRequest message) {
        var chunkRequest = message.getChunk();

        try {
            Chunk<ReaderOutputItem<Object>> inputItem = chunkRequest.getItems();
//            var processedItems = new ArrayList<PartitionedRecord>();
//
//            for (var item : inputItem.getItems()) {
//                processedItems.add(itemProcessor.process(item));
//            }
//
//            log.debug("Chunk {} processed items: {}", chunkRequest.getSequence(), processedItems);
//            recordsWriterManager.write(new Chunk<>(processedItems));

            var response = new ChunkResponse(true, chunkRequest.getSequence(), chunkRequest.getJobId(), null);
            eventSender.sendEvent(new ImportChunkMessageResponse(response));
        } catch (final Exception e) {
            log.error("Error during chunk handling", e);
            var response = new ChunkResponse(false, chunkRequest.getSequence(), chunkRequest.getJobId(), null);
            eventSender.sendEvent(new ImportChunkMessageResponse(response));
            throw new ChunkHandlingException(e);
        }
    }

    @Override
    public Class<ImportChunkMessageRequest> getMessageType() {
        return ImportChunkMessageRequest.class;
    }
}
