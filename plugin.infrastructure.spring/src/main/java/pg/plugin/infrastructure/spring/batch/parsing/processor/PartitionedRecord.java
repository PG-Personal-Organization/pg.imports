package pg.plugin.infrastructure.spring.batch.parsing.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pg.plugin.api.data.ImportedRecord;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class PartitionedRecord {
    private final ImportedRecord<?> importedRecord;
    private final String partitionId;
    private final int chunkNumber;
}
