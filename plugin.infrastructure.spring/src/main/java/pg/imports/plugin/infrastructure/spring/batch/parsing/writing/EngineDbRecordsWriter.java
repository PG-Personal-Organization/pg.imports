package pg.imports.plugin.infrastructure.spring.batch.parsing.writing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.api.writing.WrittenRecords;
import pg.imports.plugin.infrastructure.persistence.records.db.RecordEntity;
import pg.imports.plugin.infrastructure.persistence.records.db.RecordRepository;
import pg.imports.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.ERROR_STATUSES;
import static pg.imports.plugin.infrastructure.spring.batch.parsing.writing.RecordsWriterManager.SUCCESS_STATUSES;

@Log4j2
@RequiredArgsConstructor
public class EngineDbRecordsWriter implements RecordsWriter {

    private final RecordRepository recordRepository;
    private final ObjectMapper batchObjectMapper;

    @Override
    public @NonNull WrittenRecords write(final List<PartitionedRecord> records, final ImportContext importContext, final ImportPlugin plugin) {
        log.info("Writing {} records of type: {} to import database storage", records.size(), plugin.getRecordClass());
        // TODO verify if batchObjectMapper is better serializer for dynamic data in recordData
        var recordsToWrite = records.stream().map(partitionedRecord -> {
            var importedRecord = partitionedRecord.getParsedRecord();
            JsonNode jsonData = batchObjectMapper.valueToTree(importedRecord.getRecord());
            return RecordEntity.builder()
                    .importId(importedRecord.getImportId())
                    .recordStatus(importedRecord.getRecordStatus())
                    .ordinal(Math.toIntExact(importedRecord.getOrdinal()))
                    .recordType(importedRecord.getRecord().getClass().getName())
                    .recordData(jsonData)
                    .partitionId(partitionedRecord.getPartitionId())
                    .errorMessages(String.join("\n", importedRecord.getErrorMessages()))
                    .build();
        }).toList();
        recordRepository.saveAll(recordsToWrite);

        var recordsByStatus = recordsToWrite.stream()
                .collect(Collectors.groupingBy(
                        RecordEntity::getRecordStatus,
                        Collectors.mapping(r -> r, Collectors.toList())
                ));

        var errorMessages = recordsByStatus.entrySet().stream()
                .filter(entry -> ERROR_STATUSES.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toMap(r -> r.getId().toString(), RecordEntity::getErrorMessages, (l, r) -> l));

        return new WrittenRecords(getRecordIdsByStatus(recordsByStatus, SUCCESS_STATUSES), getRecordIdsByStatus(recordsByStatus, ERROR_STATUSES), errorMessages);
    }

    private List<String> getRecordIdsByStatus(final Map<ImportRecordStatus, List<RecordEntity>> records, final Set<ImportRecordStatus> statuses) {
        return records.entrySet().stream()
                .filter(entry -> statuses.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .map(RecordEntity::getId)
                .map(UUID::toString)
                .toList();
    }

    @Override
    public @NonNull RecordsStoringStrategy getRecordsStoringStrategy() {
        return RecordsStoringStrategy.LIBRARY_JSON_DATABASE;
    }
}