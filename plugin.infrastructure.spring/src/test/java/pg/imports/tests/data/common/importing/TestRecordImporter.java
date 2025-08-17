package pg.imports.tests.data.common.importing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.importing.ImportingResult;
import pg.imports.plugin.api.importing.RecordImporter;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.tests.data.common.TestRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class TestRecordImporter implements RecordImporter<TestRecord, ParsedRecord<TestRecord>> {
    private final InMemoryImportedPaymentsRepository paymentsRepository;

    @Override
    public ImportingResult importRecords(final ImportingRecordsProvider<ParsedRecord<TestRecord>> provider) {
        List<ParsedRecord<TestRecord>> records = provider.getRecords();

        var payments = records.stream().map(this::toImportedPayment).toList();
        paymentsRepository.saveAll(payments);
        log.info("Imported payments {}", payments);
        return ImportingResult.success();
    }

    private ImportedPayment toImportedPayment(final ParsedRecord<TestRecord> parsedRecord) {
        var data = parsedRecord.getRecord();
        return ImportedPayment.builder()
                .id(UUID.randomUUID())
                .name(data.getName())
                .value(data.getValue())
                .orderId(data.getOrderId())
                .importId(parsedRecord.getImportId())
                .importedOn(LocalDateTime.now())
                .build();
    }


}
