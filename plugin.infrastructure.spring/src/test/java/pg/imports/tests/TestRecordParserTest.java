package pg.imports.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pg.imports.tests.data.TestRecord;
import pg.imports.tests.data.TestRecordParser;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.parsing.ParsedRecord;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TestRecordParserTest {

    private TestRecordParser parser;
    private String importId;

    @BeforeEach
    void setUp() {
        parser = new TestRecordParser();
        importId = UUID.randomUUID().toString();
    }

    @Test
    void shouldParseValidRecord() {
        // given
        String validData = "nazwa,wartość,123";

        // when
        ParsedRecord<?> record = parser.parse(validData, importId, 1);

        // then
        assertNotNull(record);
        assertEquals(ImportRecordStatus.PARSED, record.getRecordStatus());
        assertTrue(record.getErrorMessages().isEmpty());
        assertNotNull(record.getRecord());
        assertEquals(importId, record.getImportId());
        assertEquals(1L, record.getOrdinal());

        TestRecord data = (TestRecord) record.getRecord();
        assertEquals("nazwa", data.getName());
        assertEquals("wartość", data.getValue());
        assertEquals(123, data.getOrderId());
    }

    @Test
    void shouldHandleInvalidFormat() {
        // given
        String invalidData = "tylko_jedna_część";

        // when
        ParsedRecord<?> record = parser.parse(invalidData, importId, 2);

        // then
        assertNotNull(record);
        assertEquals(ImportRecordStatus.PARSING_FAILED, record.getRecordStatus());
        assertFalse(record.getErrorMessages().isEmpty());
        assertNull(record.getRecord());
        assertEquals(importId, record.getImportId());
        assertEquals(2L, record.getOrdinal());
    }

    @Test
    void shouldHandleInvalidNumber() {
        // given
        String invalidNumberData = "nazwa,wartość,nie_liczba";

        // when
        ParsedRecord<?> record = parser.parse(invalidNumberData, importId, 3);

        // then
        assertNotNull(record);
        assertEquals(ImportRecordStatus.PARSING_FAILED, record.getRecordStatus());
        assertFalse(record.getErrorMessages().isEmpty());
        assertNull(record.getRecord());
    }

    private static Stream<Arguments> provideInvalidInputs() {
        return Stream.of(
                Arguments.of("", "Pusty ciąg znaków"),
                Arguments.of("jedna", "Za mało części"),
                Arguments.of("a,b,x", "Nieprawidłowa liczba"),
                Arguments.of(null, "Null value")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("provideInvalidInputs")
    void shouldHandleVariousInvalidInputs(String input, String testName) {
        // when
        ParsedRecord<?> record = parser.parse(input, importId, 0);

        // then
        assertEquals(ImportRecordStatus.PARSING_FAILED, record.getRecordStatus());
        assertFalse(record.getErrorMessages().isEmpty());
    }
}
