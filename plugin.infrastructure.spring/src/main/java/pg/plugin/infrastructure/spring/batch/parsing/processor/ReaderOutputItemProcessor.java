package pg.plugin.infrastructure.spring.batch.parsing.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import pg.plugin.api.parsing.ReaderOutputItem;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;

@Log4j2
@RequiredArgsConstructor
public class ReaderOutputItemProcessor implements ItemProcessor<ReaderOutputItem<Object>, PartitionedRecord> {
    private final StepExecution stepExecution;
    private final PluginCache pluginCache;

    @Override
    @SuppressWarnings("unchecked")
    public PartitionedRecord process(final @NonNull ReaderOutputItem<Object> item) {
        var importContext = JobUtil.getImportContext(stepExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());

        var recordParser = plugin.getParsingComponentProvider().getRecordParser();

        try {
            log.info("Processing item: {}", item);
            var parsedRecord = recordParser.parse(item);
            log.info("Parsed item: {}", parsedRecord);
            return PartitionedRecord.of(parsedRecord, item.getPartitionId(), item.getChunkNumber());
        } catch (final Exception e) {
            log.error("Error during record parsing", e);
            JobUtil.putRejectReason(stepExecution, String.format("Record parsing exception: %s", e.getMessage()));
            throw e;
        }
    }
}
