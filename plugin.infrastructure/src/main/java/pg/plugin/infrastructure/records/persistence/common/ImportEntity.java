package pg.plugin.infrastructure.records.persistence.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "imports")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ImportEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime startedParsingOn;

    private LocalDateTime endedParsingOn;

    private LocalDateTime startedImportingOn;

    private LocalDateTime finishedImportingOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    @Column(nullable = false)
    private UUID fileId;

    @Column(nullable = false)
    private String plugin;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private List<ImportRecordsEntity> records;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportEntity that = (ImportEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(createdOn, that.createdOn) && Objects.equals(startedParsingOn, that.startedParsingOn)
                && Objects.equals(endedParsingOn, that.endedParsingOn) && Objects.equals(startedImportingOn, that.startedImportingOn)
                && Objects.equals(finishedImportingOn, that.finishedImportingOn) && status == that.status && Objects.equals(fileId, that.fileId)
                && Objects.equals(plugin, that.plugin) && Objects.equals(userId, that.userId) && Objects.equals(records, that.records);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
