package pg.imports.tests.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.tests.data.TestParsingComponentsProvider;
import pg.imports.tests.data.TestPlugin;
import pg.imports.tests.data.TestRecord;
import pg.imports.tests.data.TestRecordParser;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.parsing.ReaderOutputItem;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TestPluginMockTest {

    private TestPlugin testPlugin;
    private String importId;

    @BeforeEach
    void setUp() {
        importId = UUID.randomUUID().toString();

        TestParsingComponentsProvider parsingComponentsProvider = new TestParsingComponentsProvider(new TestRecordParser());

        testPlugin = new TestPlugin(parsingComponentsProvider, null);
    }

    @Test
    void shouldReturnCorrectPluginCode() {
        // when
        var code = testPlugin.getCode();

        // then
        assertNotNull(code);
        assertEquals("TEST", code.code());
    }

    @Test
    void shouldParseRecordsCorrectly() {
        // given
        ReaderOutputItem<Object> recordData = ReaderOutputItem.builder()
                .chunkNumber(1)
                .id("1")
                .itemNumber(1)
                .partitionId("1")
                .rawItem(TestRecord.builder().name("test1").value(BigDecimal.valueOf(3.20)).orderId(1).build())
                .build();

        var recordParser = testPlugin.getParsingComponentProvider().getRecordParser();
        var importContext = ImportContext.of(new ImportId(importId), testPlugin.getCode(), UUID.randomUUID(), RecordsStoringStrategy.LIBRARY_JSON_DATABASE);
        assertNotNull(recordParser);

        ParsedRecord<TestRecord> parsedRecord = recordParser.parse(recordData, importContext);
        assertNotNull(parsedRecord);
        assertEquals(ImportRecordStatus.PARSED, parsedRecord.getRecordStatus());

        TestRecord data = parsedRecord.getRecord();
        assertEquals("test1", data.getName());
        assertEquals(BigDecimal.valueOf(3.20), data.getValue());
        assertEquals(1, data.getOrderId());
    }

    @Test
    void shouldReturnCorrectPluginConfig() {
        // when & then
        assertEquals("1.0.0", testPlugin.getVersion());
        assertEquals("TEST", testPlugin.getCodeIdPrefix());
        assertEquals(10, testPlugin.getChunkSize());
        assertEquals(TestRecord.class, testPlugin.getRecordClass());
    }
}
