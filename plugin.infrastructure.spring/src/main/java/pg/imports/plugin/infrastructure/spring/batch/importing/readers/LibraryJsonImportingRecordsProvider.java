package pg.imports.plugin.infrastructure.spring.batch.importing.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;
import pg.imports.plugin.infrastructure.persistence.records.db.RecordEntity;
import pg.imports.plugin.infrastructure.persistence.records.db.RecordRepository;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LibraryJsonImportingRecordsProvider implements ImportingRecordsProvider<ReadOnlyParsedRecord<RecordData>> {
    private final RecordRepository recordRepository;
    private final ObjectMapper batchObjectMapper;

    @Override
    public List<ReadOnlyParsedRecord<RecordData>> getRecords(final List<String> recordIds) {
        List<UUID> uuidIds = recordIds.stream()
                .map(UUID::fromString)
                .toList();

        Map<UUID, RecordEntity> recordEntities = recordRepository.findAllById(uuidIds).stream()
                .collect(Collectors.toMap(RecordEntity::getId, entity -> entity));

        return recordIds.stream()
                .map(UUID::fromString)
                .map(recordEntities::get)
                .filter(Objects::nonNull)
                .map(this::toParsedRecord)
                .toList();
    }

    @SneakyThrows
    private ReadOnlyParsedRecord<RecordData> toParsedRecord(final RecordEntity recordEntity) {
        Class<?> clazz = Class.forName(recordEntity.getRecordType());
        RecordData data = (RecordData) batchObjectMapper.treeToValue(recordEntity.getRecordData(), clazz);
        return ReadOnlyParsedRecord.builder()
                .importId(recordEntity.getImportId())
                .id(recordEntity.getId().toString())
                .recordData(data)
                .recordStatus(recordEntity.getRecordStatus())
                .ordinal(recordEntity.getOrdinal())
                .errorMessages(Arrays.stream(recordEntity.getErrorMessages().split("\n")).toList())
                .build();

    }
}
