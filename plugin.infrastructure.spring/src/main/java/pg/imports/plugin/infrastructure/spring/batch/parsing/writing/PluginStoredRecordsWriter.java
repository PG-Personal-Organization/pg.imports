package pg.imports.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.writing.PluginRecordsWriter;
import pg.imports.plugin.api.writing.WrittenRecords;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.strategies.self.SelfStoringRecordsPlugin;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class PluginStoredRecordsWriter implements RecordsWriter {

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull WrittenRecords write(final List<PartitionedRecord> records, final ImportContext importContext, final ImportPlugin plugin) {
        if (plugin instanceof SelfStoringRecordsPlugin<?> selfStoredPlugin) {
            PluginRecordsWriter recordsWriter = selfStoredPlugin.getRecordsWriter();
            var recordsToWrite = records.stream().map(PartitionedRecord::getParsedRecord).toList();
            log.info("Writing:{} records of type: {} to plugin {} storage", recordsToWrite.size(), plugin.getRecordClass(), plugin.getClass().getName());
            return recordsWriter.writeRecords(recordsToWrite, importContext);
        }
        throw new UnsupportedOperationException("Plugin " + plugin.getClass().getName() + " does not support storing records");
    }

    @Override
    public void writeImportingRecordErrors(final Map<String, String> recordsErrorMessages, final ImportPlugin plugin) {
        if (plugin instanceof SelfStoringRecordsPlugin<?> selfStoredPlugin) {
            var recordsWriter = selfStoredPlugin.getRecordsWriter();
            log.info("Writing {} error messages to plugin {} storage", recordsErrorMessages.size(), plugin.getClass().getName());
            recordsErrorMessages.forEach(recordsWriter::writeRecordError);
        }
        throw new UnsupportedOperationException("Plugin " + plugin.getClass().getName() + " does not support storing records");
    }

    @Override
    public @NonNull RecordsStoringStrategy getRecordsStoringStrategy() {
        return RecordsStoringStrategy.PLUGIN_DATABASE;
    }
}
