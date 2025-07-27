package pg.plugin.infrastructure.persistence.imports;

import jakarta.persistence.*;
import lombok.*;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.data.PluginCode;
import pg.plugin.infrastructure.states.*;
import pg.plugin.infrastructure.persistence.records.ImportRecordsEntity;

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
public class ImportEntity implements NewImport, OngoingParsingImport, ParsingCompletedImport, RejectedImport, OngoingImportingImport, ImportingCompletedImport, Import {
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
    private String pluginCode;

    @Column
    private UUID userId;

    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private List<ImportRecordsEntity> records;

    private String rejectedReason;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportEntity that = (ImportEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(createdOn, that.createdOn) && Objects.equals(startedParsingOn, that.startedParsingOn)
                && Objects.equals(endedParsingOn, that.endedParsingOn) && Objects.equals(startedImportingOn, that.startedImportingOn)
                && Objects.equals(finishedImportingOn, that.finishedImportingOn) && status == that.status && Objects.equals(fileId, that.fileId)
                && Objects.equals(pluginCode, that.pluginCode) && Objects.equals(userId, that.userId) && Objects.equals(records, that.records)
                && Objects.equals(rejectedReason, that.rejectedReason);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public OngoingParsingImport startParsing() {
        this.status = ImportStatus.ONGOING_PARSING;
        this.startedParsingOn = LocalDateTime.now();
        return this;
    }

    @Override
    public ImportId getImportId() {
        return new ImportId(id);
    }

    @Override
    public PluginCode getPluginCode() {
        return new PluginCode(pluginCode);
    }

    @Override
    public RejectedImport rejectParsing(final String reason) {
        this.status = ImportStatus.PARSING_FAILED;
        this.rejectedReason = reason;
        this.endedParsingOn = LocalDateTime.now();
        return this;
    }

    @Override
    public ParsingCompletedImport finishParsing() {
        this.status = ImportStatus.PARSING_FINISHED;
        this.endedParsingOn = LocalDateTime.now();
        return this;
    }

    @Override
    public ImportingCompletedImport finishImporting() {
        this.status = ImportStatus.IMPORTING_COMPLETED;
        this.finishedImportingOn = LocalDateTime.now();
        return this;
    }

    @Override
    public RejectedImport rejectImporting(final String reason) {
        this.status = ImportStatus.IMPORTING_FAILED;
        this.rejectedReason = reason;
        return this;
    }

    @Override
    public OngoingImportingImport startImporting() {
        this.startedImportingOn = LocalDateTime.now();
        this.status = ImportStatus.ONGOING_IMPORTING;
        return this;
    }
}
