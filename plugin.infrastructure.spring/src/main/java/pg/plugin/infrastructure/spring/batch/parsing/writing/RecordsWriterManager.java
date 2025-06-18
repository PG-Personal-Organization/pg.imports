package pg.plugin.infrastructure.spring.batch.parsing.writing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.rejection.reason.ImportRejectionReasons;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.spring.batch.JobUtil;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;
import java.util.Set;

import static pg.plugin.api.data.ImportRecordStatus.*;
import static pg.plugin.api.data.ImportRecordStatus.IMPORTED;

@Log4j2
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class RecordsWriterManager implements ItemWriter<PartitionedRecord> {
    public static final Set<ImportRecordStatus> ERROR_STATUSES =
            Set.of(PARSING_FAILED, IMPORTING_FAILED);
    public static final Set<ImportRecordStatus> SUCCESS_STATUSES =
            Set.of(PARSED, IMPORTED);

    @NonNull
    private final StepExecution stepExecution;
    @NonNull
    private final PluginCache pluginCache;
    @NonNull
    private final List<RecordsWriter> recordsWriters;
    @NonNull
    private final RecordsRepository recordsRepository;
    @NonNull
    private final ImportRepository importRepository;

    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public void write(final Chunk<? extends PartitionedRecord> chunk) {
        var importContext = JobUtil.getImportContext(stepExecution);
        var recordsStoringStrategy = JobUtil.getRecordsStoringStrategy(stepExecution);
        var plugin = pluginCache.getPlugin(importContext.getPluginCode());
        var inProgressImport = importRepository.getParsingImport(importContext.getImportId().id());

        try {
            var recordsWriter = getRecordsWriter(recordsStoringStrategy);
            var items = (List<PartitionedRecord>) chunk.getItems();
            var records = recordsWriter.write(items, importContext, plugin);
            var recordsEntity = ImportRecordsEntity.from(
                    inProgressImport,
                    chunk.getItems().getFirst().getPartitionId(),
                    records.recordIds(),
                    records.errorRecordIds(),
                    records.errorMessages()
            );
            recordsRepository.save(recordsEntity);
        } catch (Exception e) {
            log.error("Item Writer error with import id {}", importContext.getImportId(), e);
            stepExecution.getExecutionContext().put(JobUtil.REJECT_REASON_KEY, String.format("%s:\n%s", ImportRejectionReasons.UNEXPECTED, e.getMessage()));
            throw e;
        }
    }

    private RecordsWriter getRecordsWriter(final RecordsStoringStrategy recordsStoringStrategy) {
        return recordsWriters.stream().filter(recordsWriter -> recordsWriter.getRecordsStoringStrategy().equals(recordsStoringStrategy)).findFirst().orElseThrow();
    }
}
