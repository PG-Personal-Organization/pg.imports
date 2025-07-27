package pg.plugin.infrastructure.spring.batch.importing.readers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pg.plugin.api.importing.ImportingRecordsProvider;
import pg.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.plugin.api.strategies.db.RecordData;
import pg.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.plugin.infrastructure.persistence.records.mongo.RecordDocument;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoImportingRecordsProvider implements ImportingRecordsProvider<ReadOnlyParsedRecord> {
    private final MongoRecordRepository recordRepository;
    private final ObjectMapper batchObjectMapper;

    @Override
    @SneakyThrows
    public List<ReadOnlyParsedRecord> getRecords(final List<String> recordIds) {
        var uuidIds = recordIds.stream()
                .map(UUID::fromString)
                .toList();

        Map<UUID, RecordDocument> recordDocuments = recordRepository.findAllById(uuidIds).stream()
                .collect(Collectors.toMap(RecordDocument::getId, document -> document));

        return recordIds.stream()
                .map(UUID::fromString)
                .map(recordDocuments::get)
                .filter(Objects::nonNull)
                .map(this::toParsedRecord)
                .toList();
    }

    @SneakyThrows
    private ReadOnlyParsedRecord toParsedRecord(final RecordDocument recordDocument) {
        return ReadOnlyParsedRecord.builder()
                .importId(recordDocument.getImportId())
                .id(recordDocument.getId().toString())
                .recordData((RecordData) batchObjectMapper.readValue(recordDocument.getRecordData(), recordDocument.getRecordDataClass()))
                .recordStatus(recordDocument.getRecordStatus())
                .ordinal(recordDocument.getOrdinal())
                .errorMessages(Arrays.stream(recordDocument.getErrorMessages().split("\n")).toList())
                .build();

    }
}
