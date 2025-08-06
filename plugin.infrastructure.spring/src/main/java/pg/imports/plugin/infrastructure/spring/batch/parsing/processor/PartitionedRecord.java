package pg.imports.plugin.infrastructure.spring.batch.parsing.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.parsing.ParsedRecord;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class PartitionedRecord {
    private final ParsedRecord<?> parsedRecord;
    private final String partitionId;
    private final int chunkNumber;
}
