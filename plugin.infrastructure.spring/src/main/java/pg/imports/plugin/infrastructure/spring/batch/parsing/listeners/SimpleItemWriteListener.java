package pg.imports.plugin.infrastructure.spring.batch.parsing.listeners;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

@Log4j2
public class SimpleItemWriteListener implements ItemWriteListener<PartitionedRecord> {
    @Override
    public void onWriteError(final @NonNull Exception exception, final Chunk items) {
        log.error("Write failed for {} items. First item: {}", items.size(), items.isEmpty() ? null : items.getItems().getFirst(), exception);
    }
}
