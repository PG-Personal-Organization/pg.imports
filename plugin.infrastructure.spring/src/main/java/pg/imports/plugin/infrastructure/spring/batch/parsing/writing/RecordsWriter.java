package pg.imports.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.NonNull;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.writing.WrittenRecords;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;

public interface RecordsWriter {
    @NonNull
    WrittenRecords write(List<PartitionedRecord> records, ImportContext importContext, ImportPlugin plugin);

    @NonNull
    RecordsStoringStrategy getRecordsStoringStrategy();
}
