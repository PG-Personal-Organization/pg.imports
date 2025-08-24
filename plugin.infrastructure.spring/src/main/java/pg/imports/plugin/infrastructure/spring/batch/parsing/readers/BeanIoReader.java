package pg.imports.plugin.infrastructure.spring.batch.parsing.readers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.beanio.spring.BeanIOFlatFileItemReader;
import org.springframework.batch.core.StepExecution;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.parsing.ReaderOutputItem;

import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class BeanIoReader extends BeanIOFlatFileItemReader<ReaderOutputItem<Object>> {
    private final ImportPlugin importPlugin;
    private final ImportContext importContext;
    private final StepExecution stepExecution;

    @Override
    protected ReaderOutputItem doRead() {
        final int currentItemCount = getCurrentItemCount();
        try {
            Object record = super.doRead();
            if (record == null) {
                return null;
            }

            int chunkNumber = getChunkNumber();
            String partitionId = calculatePartitionId(chunkNumber);
            return ReaderOutputItem.builder()
                    .id(importPlugin.getRecordsPrefix() + UUID.randomUUID())
                    .itemNumber(currentItemCount)
                    .rawItem(record)
                    .partitionId(partitionId)
                    .chunkNumber(chunkNumber)
                    .build();
        } catch (Exception e) {
            log.error("BeanIoReader error with import id: {}", importContext.getImportId(), e);
            throw new ReadItemException("Incorrect item: " + currentItemCount + ", errorMessage: " + e.getMessage());
        }
    }

    @Override
    protected void jumpToItem(final int itemIndex) throws Exception {
        try {
            super.jumpToItem(itemIndex);
        } catch (IllegalStateException e) {
            log.info("Illegal state of reader, probably end of file.", e);
        }
    }

    private String calculatePartitionId(final int chunkNumber) {
        return String.valueOf(chunkNumber / importPlugin.getChunkSizeMultiplierForPartitionSize());
    }

    private int getChunkNumber() {
        return getCurrentItemCount() / importPlugin.getChunkSize();
    }
}
