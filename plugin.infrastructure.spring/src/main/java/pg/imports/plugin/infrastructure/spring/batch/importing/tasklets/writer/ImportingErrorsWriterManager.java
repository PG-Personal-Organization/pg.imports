package pg.imports.plugin.infrastructure.spring.batch.importing.tasklets.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriter;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ImportingErrorsWriterManager {
    private final List<RecordsWriter> recordsWriters;

    public void writeErrors(final RecordsStoringStrategy storingStrategy, final @NonNull Map<String, String> errorMessages, final ImportPlugin plugin) {
        getRecordsWriter(storingStrategy).writeImportingRecordErrors(errorMessages, plugin);
    }

    private RecordsWriter getRecordsWriter(final @NonNull RecordsStoringStrategy recordsStoringStrategy) {
        return recordsWriters.stream().filter(recordsWriter -> recordsWriter.getRecordsStoringStrategy().equals(recordsStoringStrategy)).findFirst().orElseThrow();
    }
}
