package pg.imports.tests.data.common.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.beanio.builder.CsvParserBuilder;
import org.beanio.builder.StreamBuilder;
import pg.imports.plugin.api.parsing.*;
import pg.imports.tests.data.common.TestRecord;

@RequiredArgsConstructor
public class TestParsingComponentsProvider implements ParsingComponentsProvider<TestRecord, ParsedRecord<TestRecord>> {

    private final RecordParser<TestRecord, ParsedRecord<TestRecord>> recordParser;

    @NonNull
    @Override
    public ReaderDefinition getReaderDefinition() {
        return BeanIoReaderDefinition.builder()
                .name("testReader")
                .streamBuilder(new StreamBuilder("testReader")
                        .format("csv")
                        .parser(new CsvParserBuilder().delimiter(','))
                        .addRecord(TestRecord.class)
                )
                .build();
    }

    @NonNull
    @Override
    public RecordParser<TestRecord, ParsedRecord<TestRecord>> getRecordParser() {
        return recordParser;
    }
}
