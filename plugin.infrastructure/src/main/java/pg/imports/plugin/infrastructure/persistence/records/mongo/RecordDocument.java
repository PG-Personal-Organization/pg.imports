package pg.imports.plugin.infrastructure.persistence.records.mongo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.strategies.mongo.LibraryMongoStoredRecordsPlugin;

import java.util.UUID;

@SuperBuilder
@Data
@Document(collection = "import_records")
public class RecordDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String importId;

    private int ordinal;

    private String partitionId;

    /**
    * De/Serialized via {@link LibraryMongoStoredRecordsPlugin#getRecordClass()}
    */
    private String recordData;

    private Class<?> recordDataClass;

    @Field(name = "record_status")
    private ImportRecordStatus recordStatus;

    private String errorMessages;
}
