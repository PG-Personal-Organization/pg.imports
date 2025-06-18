package pg.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.records.writing.WrittenRecords;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.api.strategies.self.SelfStoringRecordsPlugin;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class PluginStoredRecordsWriter implements RecordsWriter {

    @Override
    public @NonNull WrittenRecords write(final List<PartitionedRecord> records, final ImportContext importContext, final ImportPlugin plugin) {
        if (plugin instanceof SelfStoringRecordsPlugin<?> selfStoredPlugin) {
            var recordsWriter = selfStoredPlugin.getRecordsWriter();
            var recordsToWrite = records.stream().map(PartitionedRecord::getImportedRecord).toList();
            log.info("Writing:{} records of type: {} to plugin {} storage", recordsToWrite.size(), plugin.getRecordClass(), plugin.getClass().getName());
            return recordsWriter.writeRecords(recordsToWrite, importContext);
        }
        throw new UnsupportedOperationException("Plugin " + plugin.getClass().getName() + " does not support storing records");
    }

    @Override
    public @NonNull RecordsStoringStrategy getRecordsStoringStrategy() {
        return RecordsStoringStrategy.PLUGIN_DATABASE;
    }
}
