package pg.imports.plugin.infrastructure.spring.batch.parsing.writing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.writing.WrittenRecords;
import pg.imports.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.imports.plugin.infrastructure.persistence.records.mongo.RecordDocument;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.ERROR_STATUSES;
import static pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.SUCCESS_STATUSES;

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
                        .id(importedRecord.getRecordId())
                        .importId(importedRecord.getImportId())
                        .recordStatus(importedRecord.getRecordStatus())
                        .ordinal(Math.toIntExact(importedRecord.getOrdinal()))
                        .recordData(batchObjectMapper.writeValueAsString(importedRecord.getRecord()))
                        .recordDataClass(plugin.getRecordClass().getName())
                        .partitionId(partitionedRecord.getPartitionId())
                        .errorMessages(String.join("\n", importedRecord.getErrorMessages()))
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Error during converting record: {} to mongo storage document", partitionedRecord,  e);
                return RecordDocument.builder()
                        .id(importedRecord.getRecordId())
                        .importId(importedRecord.getImportId())
                        .recordStatus(ImportRecordStatus.PARSING_FAILED)
                        .ordinal(Math.toIntExact(importedRecord.getOrdinal()))
                        .recordDataClass(plugin.getRecordClass().getName())
                        .partitionId(partitionedRecord.getPartitionId())
                        .errorMessages(e.getMessage())
                        .build();
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
                .collect(Collectors.toMap(RecordDocument::getId, RecordDocument::getErrorMessages, (l, r) -> l));

        return new WrittenRecords(getRecordIdsByStatus(recordsByStatus, SUCCESS_STATUSES), getRecordIdsByStatus(recordsByStatus, ERROR_STATUSES), errorMessages);
    }

    @Override
    public void writeImportingRecordErrors(final Map<String, String> recordsErrorMessages, final ImportPlugin plugin) {
        var records = recordRepository.findAllById(recordsErrorMessages.keySet());
        if (records.size() != recordsErrorMessages.size()) {
            throw new IllegalStateException("Some records were not found in database storage");
        }
        records.forEach(record -> record.setErrorMessages(record.getErrorMessages().concat("\n" + recordsErrorMessages.get(record.getId()))));
        recordRepository.saveAll(records);
    }

    private List<String> getRecordIdsByStatus(final Map<ImportRecordStatus, List<RecordDocument>> records, final Set<ImportRecordStatus> statuses) {
        return records.entrySet().stream()
                .filter(entry -> statuses.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .map(RecordDocument::getId)
                .toList();
    }

    @Override
    public @NonNull RecordsStoringStrategy getRecordsStoringStrategy() {
        return RecordsStoringStrategy.MONGO_REPOSITORY;
    }
}
