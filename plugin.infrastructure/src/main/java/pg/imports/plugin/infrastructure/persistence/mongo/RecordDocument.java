package pg.imports.plugin.infrastructure.persistence.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.ImportPlugin;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "import_records")
public class RecordDocument {
    @Id
    private String id;

    private String importId;

    private int ordinal;

    private String partitionId;

    private Map<String, Object> recordData;

    private String recordDataClass;

    @Field(name = "record_status")
    private ImportRecordStatus recordStatus;

    private String errorMessages;
}
