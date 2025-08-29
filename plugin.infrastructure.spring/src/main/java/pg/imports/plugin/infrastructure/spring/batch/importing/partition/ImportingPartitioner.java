package pg.imports.plugin.infrastructure.spring.batch.importing.partition;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.infrastructure.persistence.database.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ImportingPartitioner implements Partitioner {
    public static final String PARTITION_KEY = "partition";
    public static final String PARTITION_CONTEXT_KEY = "partitionContext_";

    private final RecordsRepository recordsRepository;
    private final ImportId importId;

    @NonNull
    @Override
    public Map<String, ExecutionContext> partition(final int gridSize) {
        var partitionIds = recordsRepository.findAllByParent_Id(importId.id()).stream().map(ImportRecordsEntity::getId).toList();
        Map<String, ExecutionContext> map = HashMap.newHashMap(partitionIds.size());
        for (int i = 0; i < partitionIds.size(); i++) {
            var context = new ExecutionContext();
            context.put(PARTITION_KEY, partitionIds.get(i));
            map.put(PARTITION_CONTEXT_KEY + i, context);
        }
        return map;
    }
}
