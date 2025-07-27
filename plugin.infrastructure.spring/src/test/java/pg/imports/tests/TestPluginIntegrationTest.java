package pg.imports.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pg.imports.tests.data.TestParsingComponentsProvider;
import pg.imports.tests.data.TestPlugin;
import pg.imports.tests.data.TestRecordData;
import pg.imports.tests.data.TestRecordParser;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.importing.ImportingComponentsProvider;
import pg.plugin.api.importing.ImportingRecordsProvider;
import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.parsing.ReaderDefinition;
import pg.plugin.api.parsing.RecordsParsingErrorHandler;
import pg.plugin.api.strategies.db.RecordData;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TestPluginIntegrationTest {

    @Mock
    private ImportingComponentsProvider<TestRecordData, ParsedRecord<RecordData>, ImportingRecordsProvider<ParsedRecord<RecordData>>> importingProvider;

    private TestRecordParser recordParser;
    private TestPlugin testPlugin;
    private String importId;

    @BeforeEach
    void setUp() {
        importId = UUID.randomUUID().toString();
        recordParser = new TestRecordParser();

        ReaderDefinition readerDefinition = new ReaderDefinition();
        RecordsParsingErrorHandler errorHandler = recordIds -> {};

        TestParsingComponentsProvider parsingProvider = new TestParsingComponentsProvider(
                recordParser, readerDefinition, errorHandler);

        testPlugin = new TestPlugin(parsingProvider, importingProvider);
    }

    @Test
    void shouldReturnCorrectPluginCode() {
        // when
        var code = testPlugin.getCode();

        // then
        assertNotNull(code);
        assertEquals("TEST", code.getCode());
    }

    @Test
    void shouldParseRecordsCorrectly() {
        // given
        String[] testData = {
            "test1,value1,1",
            "test2,value2,2",
            "test3,value3,3"
        };

        // when
        List<ParsedRecord<TestRecordData>> parsedRecords = Arrays.stream(testData)
                .map(data -> recordParser.parse(data, importId, Arrays.asList(testData).indexOf(data)))
                .toList();

        // then
        assertEquals(3, parsedRecords.size());
        parsedRecords.forEach(record -> {
            assertEquals(ImportRecordStatus.PARSED, record.getRecordStatus());
            assertTrue(record.getErrorMessages().isEmpty());
        });

        var recordParser = testPlugin.getParsingComponentProvider().getRecordParser();
        assertNotNull(recordParser);

        ParsedRecord<TestRecordData> parsedRecord = recordParser.parse("test4,value4,4", importId, 0);
        assertNotNull(parsedRecord);
        assertEquals(ImportRecordStatus.PARSED, parsedRecord.getRecordStatus());

        TestRecordData data = (TestRecordData) parsedRecord.getRecord();
        assertEquals("test4", data.getName());
        assertEquals("value4", data.getValue());
        assertEquals(4, data.getOrderId());
    }

    @Test
    void shouldHandleParsingErrors() {
        // given
        var recordParser = testPlugin.getParsingComponentProvider().getRecordParser();

        // when
        ParsedRecord<TestRecordData> parsedRecord = recordParser.parse("invalid_format", importId, 0);

        // then
        assertNotNull(parsedRecord);
        assertEquals(ImportRecordStatus.PARSING_FAILED, parsedRecord.getRecordStatus());
        assertFalse(parsedRecord.getErrorMessages().isEmpty());
        assertNull(parsedRecord.getRecord());
    }

    @Test
    void shouldReturnCorrectPluginConfig() {
        // when & then
        assertEquals("1.0.0", testPlugin.getVersion());
        assertEquals("TEST", testPlugin.getCodeIdPrefix());
        assertEquals(10, testPlugin.getChunkSize());
        assertEquals(TestRecordData.class, testPlugin.getRecordClass());
    }
}
