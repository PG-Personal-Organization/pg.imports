package pg.plugin.infrastructure.records.persistence.common;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "imported_records")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ImportRecordsEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime finishedParsingOn;

    private LocalDateTime startedImportingOn;

    private LocalDateTime finishedImportingOn;

    @ElementCollection
    private Set<String> recordIds = new HashSet<>();

    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private ImportRecordsEntity parent;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportRecordsEntity that = (ImportRecordsEntity) o;
        return count == that.count && Objects.equals(id, that.id) && Objects.equals(createdOn, that.createdOn) && Objects.equals(finishedParsingOn, that.finishedParsingOn)
                && Objects.equals(startedImportingOn, that.startedImportingOn) && Objects.equals(finishedImportingOn, that.finishedImportingOn)
                && Objects.equals(recordIds, that.recordIds) && Objects.equals(parent, that.parent);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
