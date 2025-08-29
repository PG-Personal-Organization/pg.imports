package pg.imports.plugin.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportRecordsPartition implements Serializable {
    private String id;

    private String partitionNumber;

    private LocalDateTime finishedParsingOn;

    private LocalDateTime startedImportingOn;

    private LocalDateTime finishedImportingOn;

    private RecordsStatus recordsStatus;

    private Set<String> successRecordIds = new HashSet<>();

    private Set<String> errorRecordIds = new HashSet<>();

    private int count;

    private int errorCount;

    private Map</* recordId*/String, /* errorMessages*/String> errorMessages = new HashMap<>();

    private String importId;

    private RecordsStoringStrategy strategy;
}
