package pg.imports.plugin.infrastructure.persistence.records;


import jakarta.persistence.*;
import lombok.*;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.states.OngoingParsingImport;

import java.time.LocalDateTime;
import java.util.*;

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
    private String partitionNumber;

    @Column(nullable = false)
    private LocalDateTime finishedParsingOn;

    private LocalDateTime startedImportingOn;

    private LocalDateTime finishedImportingOn;

    @ElementCollection
    private Set<String> recordIds = new HashSet<>();

    @ElementCollection
    private Set<String> errorRecordIds = new HashSet<>();

    private int count;

    private int errorCount;

    @ElementCollection
    private Map</* recordId*/String, /* errorMessages*/String> errorMessages = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private ImportEntity parent;

    @Enumerated(EnumType.STRING)
    private RecordsStoringStrategy strategy;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportRecordsEntity that = (ImportRecordsEntity) o;
        return count == that.count && errorCount == that.errorCount && Objects.equals(id, that.id) && Objects.equals(partitionNumber, that.partitionNumber)
                && Objects.equals(finishedParsingOn, that.finishedParsingOn) && Objects.equals(startedImportingOn, that.startedImportingOn)
                && Objects.equals(finishedImportingOn, that.finishedImportingOn) && Objects.equals(recordIds, that.recordIds)
                && Objects.equals(errorRecordIds, that.errorRecordIds) && Objects.equals(errorMessages, that.errorMessages) && Objects.equals(parent, that.parent)
                && Objects.equals(strategy, that.strategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, partitionNumber, finishedParsingOn, startedImportingOn, finishedImportingOn, recordIds, errorRecordIds, count, errorCount, errorMessages, parent,
                strategy);
    }

    public static ImportRecordsEntity from(final OngoingParsingImport parsingImport,
                                           final String partitionNumber,
                                           final List<String> recordIds,
                                           final List<String> errorRecordIds,
                                           final Map<String, String> errorMessages,
                                           final RecordsStoringStrategy strategy) {
        return ImportRecordsEntity.builder()
                .id(String.format("IMP_REC_%s_%s", parsingImport.getImportId(), partitionNumber))
                .parent((ImportEntity) parsingImport)
                .partitionNumber(partitionNumber)
                .recordIds(new HashSet<>(recordIds))
                .errorRecordIds(new HashSet<>(errorRecordIds))
                .errorMessages(errorMessages)
                .count(recordIds.size())
                .errorCount(errorRecordIds.size())
                .finishedParsingOn(LocalDateTime.now())
                .strategy(strategy)
                .build();
    }
}
