package pg.plugin.infrastructure.records.persistence.internal.records.db;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import pg.plugin.api.ImportRecordStatus;
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

    private int ordinal;

    @Enumerated(EnumType.STRING)
    private ImportRecordStatus recordStatus;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private RecordData recordData;

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecordEntity that = (RecordEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(recordData, that.recordData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordData);
    }
}
