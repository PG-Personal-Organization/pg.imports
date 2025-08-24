package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.Chunk;
import pg.imports.plugin.api.parsing.ReaderOutputItem;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.distributed.ChunkHandlingException;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.DistributedOutputItemProcessor;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class DistributedParseChunkRequestHandler implements MessageHandler<ParseChunkMessageRequest> {
    private final RecordsWriterManager recordsWriterManager;
    private final DistributedOutputItemProcessor itemProcessor;
    private final EventSender eventSender;
    private final ObjectMapper batchObjectMapper;
    private final PluginCache pluginCache;

    @Override
    public void handleMessage(final @NonNull ParseChunkMessageRequest message) {
        try {
            Class<?> recordClazz = pluginCache.getPlugin(message.getImportContext().getPluginCode()).getRecordClass();
            List<ReaderOutputItem<Object>> inputItems = message.getItems()
                    .stream()
                    .map(it -> batchObjectMapper.convertValue(it, new com.fasterxml.jackson.core.type.TypeReference<ReaderOutputItem<Object>>() { }))
                    .map(it -> {
                        var rawItem = batchObjectMapper.convertValue(it.getRawItem(), recordClazz);
                        return new ReaderOutputItem<>(it.getId(), it.getItemNumber(), rawItem, it.getPartitionId(), it.getChunkNumber());
                    })
                    .toList();
            var processedItems = new ArrayList<PartitionedRecord>();

            for (var item : inputItems) {
                processedItems.add(itemProcessor.process(item, message.getImportContext()));
            }

            log.debug("Chunk {} processed items: {}", message.getSequence(), processedItems);
            recordsWriterManager.write(new Chunk<>(processedItems), message.getImportContext());

            eventSender.sendEvent(new ParseChunkMessageResponse(message.getJobId(), message.getSequence(), true, null));
        } catch (final Exception e) {
            log.error("Error during chunk handling", e);
            eventSender.sendEvent(new ParseChunkMessageResponse(message.getJobId(), message.getSequence(), false, e.getMessage()));
            throw new ChunkHandlingException(e);
        }
    }

    @Override
    public Class<ParseChunkMessageRequest> getMessageType() {
        return ParseChunkMessageRequest.class;
    }
}
