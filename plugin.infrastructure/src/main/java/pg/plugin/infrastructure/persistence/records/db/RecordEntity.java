package pg.plugin.infrastructure.persistence.records.db;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import pg.plugin.api.data.ImportRecordStatus;
import pg.plugin.api.strategies.db.RecordData;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "imported_records")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String importId;

    private int ordinal;

    private String partitionId;

    @Enumerated(EnumType.STRING)
    private ImportRecordStatus recordStatus;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private RecordData recordData;

    private String errorMessages;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecordEntity record = (RecordEntity) o;
        return ordinal == record.ordinal && Objects.equals(id, record.id) && Objects.equals(importId, record.importId) && Objects.equals(partitionId, record.partitionId)
                && recordStatus == record.recordStatus && Objects.equals(recordData, record.recordData) && Objects.equals(errorMessages, record.errorMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, importId, ordinal, partitionId, recordStatus, recordData, errorMessages);
    }
}
