package pg.imports.plugin.infrastructure.spring.batch.importing.records.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.parsing.ReadOnlyParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;
import pg.imports.plugin.infrastructure.persistence.records.mongo.MongoRecordRepository;
import pg.imports.plugin.infrastructure.persistence.records.mongo.RecordDocument;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoImportingRecordsProvider implements ImportingRecordsProvider<ReadOnlyParsedRecord<RecordData>> {
    private final MongoRecordRepository recordRepository;
    private final ObjectMapper batchObjectMapper;
    private final List<String> recordIds;

    @Override
    @SneakyThrows
    public List<ReadOnlyParsedRecord<RecordData>> getRecords() {
        Map<String, RecordDocument> recordDocuments = recordRepository.findAllById(recordIds).stream()
                .collect(Collectors.toMap(RecordDocument::getId, document -> document));

        return recordIds.stream()
                .map(recordDocuments::get)
                .filter(Objects::nonNull)
                .map(this::toParsedRecord)
                .toList();
    }

    @SneakyThrows
    private ReadOnlyParsedRecord<RecordData> toParsedRecord(final RecordDocument recordDocument) {
        return ReadOnlyParsedRecord.builder()
                .importId(recordDocument.getImportId())
                .recordId(recordDocument.getId())
                .recordData((RecordData) batchObjectMapper.readValue(recordDocument.getRecordData(), Class.forName(recordDocument.getRecordDataClass())))
                .recordStatus(recordDocument.getRecordStatus())
                .ordinal(recordDocument.getOrdinal())
                .errorMessages(Arrays.stream(recordDocument.getErrorMessages().split("\n")).toList())
                .build();
    }
}
