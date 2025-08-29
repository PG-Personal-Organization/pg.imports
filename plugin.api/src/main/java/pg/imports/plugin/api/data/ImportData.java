package pg.imports.plugin.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportData implements Serializable {
    private String id;

    private LocalDateTime createdOn;

    private LocalDateTime startedParsingOn;

    private LocalDateTime endedParsingOn;

    private LocalDateTime startedImportingOn;

    private LocalDateTime finishedImportingOn;

    private ImportStatus status;

    private int countOfRecordPartitions;

    private UUID fileId;

    private String pluginCode;

    private String ownerId;

    private String rejectedReason;
}
