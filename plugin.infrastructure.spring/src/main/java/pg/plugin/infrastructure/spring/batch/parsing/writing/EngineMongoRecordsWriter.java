package pg.plugin.infrastructure.spring.batch.parsing.writing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.records.writing.WrittenRecords;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.plugin.infrastructure.persistence.records.mongo.RecordDocument;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.ERROR_STATUSES;
import static pg.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.SUCCESS_STATUSES;

@Log4j2
@RequiredArgsConstructor
public class EngineMongoRecordsWriter implements RecordsWriter {
    private final MongoRecordRepository recordRepository;
    private final ObjectMapper batchObjectMapper;

    @Override
    public @NonNull WrittenRecords write(final List<PartitionedRecord> records, final ImportContext importContext, final ImportPlugin plugin) {
        log.info("Writing {} records of type: {} to import mongo storage", records.size(), plugin.getRecordClass());
        var recordsToWrite = records.stream().map(partitionedRecord -> {
            var importedRecord = partitionedRecord.getParsedRecord();
            try {
                return RecordDocument.builder()
                        .importId(importedRecord.getImportId())
                        .recordStatus(importedRecord.getRecordStatus())
                        .ordinal(Math.toIntExact(importedRecord.getOrdinal()))
                        .recordData(batchObjectMapper.writeValueAsString(importedRecord.getRecord()))
                        .partitionId(partitionedRecord.getPartitionId())
                        .errorMessages(String.join("\n", importedRecord.getErrorMessages()))
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Error during converting record: {} to mongo storage document", partitionedRecord,  e);
                return RecordDocument.builder().build();
            }
        }).toList();
        recordRepository.saveAll(recordsToWrite);

        var recordsByStatus = recordsToWrite.stream()
                .collect(Collectors.groupingBy(
                        RecordDocument::getRecordStatus,
                        Collectors.mapping(r -> r, Collectors.toList())
                ));

        var errorMessages = recordsByStatus.entrySet().stream()
                .filter(entry -> ERROR_STATUSES.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toMap(r -> r.getId().toString(), RecordDocument::getErrorMessages, (l, r) -> l));

        return new WrittenRecords(getRecordIdsByStatus(recordsByStatus, ERROR_STATUSES), getRecordIdsByStatus(recordsByStatus, SUCCESS_STATUSES), errorMessages);
    }

    private List<String> getRecordIdsByStatus(final Map<ImportRecordStatus, List<RecordDocument>> records, final Set<ImportRecordStatus> statuses) {
        return records.entrySet().stream()
                .filter(entry -> statuses.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .map(RecordDocument::getId)
                .map(UUID::toString)
                .toList();
    }

    @Override
    public @NonNull RecordsStoringStrategy getRecordsStoringStrategy() {
        return RecordsStoringStrategy.MONGO_REPOSITORY;
    }
}
