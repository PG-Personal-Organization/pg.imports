package pg.plugin.api.strategies.mongo;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pg.plugin.api.ImportRecordStatus;

import java.util.UUID;

@SuperBuilder
@Data
@Document(collection = "import_records")
public abstract class RecordDocument {
    @Id
    protected UUID id;

    protected int ordinal;

    @Field(name = "record_status")
    protected ImportRecordStatus recordStatus;
}
