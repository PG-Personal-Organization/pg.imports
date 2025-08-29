package pg.imports.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.reason.ImportRejectionReasons;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.writing.WrittenRecords;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pg.imports.plugin.api.data.ImportRecordStatus.*;

@Log4j2
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class RecordsWriterManager implements ItemWriter<PartitionedRecord> {
    public static final Set<ImportRecordStatus> ERROR_STATUSES =
            Set.of(PARSING_FAILED, IMPORTING_FAILED);
    public static final Set<ImportRecordStatus> SUCCESS_STATUSES =
            Set.of(PARSED, IMPORTED);

    @NonNull
    private final PluginCache pluginCache;
    @NonNull
    private final List<RecordsWriter> recordsWriters;
    @NonNull
    private final RecordsRepository recordsRepository;
    @NonNull
    private final ImportRepository importRepository;

    private StepExecution stepExecution;

    @SuppressWarnings("unchecked")
    public void write(final Chunk<? extends PartitionedRecord> chunk, final ImportContext importContext) {
        var recordsStoringStrategy = importContext.getRecordsStoringStrategy();
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());
        var inProgressImport = importRepository.getParsingImport(importContext.getImportId().id());

        try {
            var recordsWriter = getRecordsWriter(recordsStoringStrategy);
            var items = (List<PartitionedRecord>) chunk.getItems();
            var records = writeRecords(importContext, recordsWriter, items, plugin);
            var recordsEntity = ImportRecordsEntity.from(
                    inProgressImport,
                    chunk.getItems().getFirst().getPartitionId(),
                    records.recordIds(),
                    records.errorRecordIds(),
                    records.errorMessages(),
                    recordsStoringStrategy
            );
            recordsRepository.save(recordsEntity);
        } catch (Exception e) {
            log.error("Item Writer error with import id {}", importContext.getImportId(), e);
            if (stepExecution != null) {
                stepExecution.getExecutionContext().put(JobUtil.REJECT_REASON_KEY, String.format("%s:\n%s", ImportRejectionReasons.UNEXPECTED, e.getMessage()));
            }
            throw e;
        }
    }

    private WrittenRecords writeRecords(final ImportContext importContext, final RecordsWriter recordsWriter, final List<PartitionedRecord> items,
                                        final ImportPlugin plugin) {
        WrittenRecords records;
        try {
            records = recordsWriter.write(items, importContext, plugin);
        } catch (final Exception e) {
            log.error("Error during records writing", e);
            var errorRecordIds = items.stream().map(PartitionedRecord::getParsedRecord).map(ParsedRecord::getRecordId).toList();
            records = new WrittenRecords(Collections.emptyList(), errorRecordIds,
                    errorRecordIds.stream().collect(Collectors.toMap(id -> id, id -> e.getMessage())));
        }
        return records;
    }

    @Override
    public void write(final @NonNull Chunk<? extends PartitionedRecord> chunk) {
        write(chunk, JobUtil.getImportContext(stepExecution));
    }

    private RecordsWriter getRecordsWriter(final @NonNull RecordsStoringStrategy recordsStoringStrategy) {
        return recordsWriters.stream().filter(recordsWriter -> recordsWriter.getRecordsStoringStrategy().equals(recordsStoringStrategy)).findFirst().orElseThrow();
    }
}
