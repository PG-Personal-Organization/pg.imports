package pg.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.NonNull;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.records.writing.WrittenRecords;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;

public interface RecordsWriter {
    @NonNull
    WrittenRecords write(List<PartitionedRecord> records, ImportContext importContext, ImportPlugin plugin);

    @NonNull
    RecordsStoringStrategy getRecordsStoringStrategy();
}
