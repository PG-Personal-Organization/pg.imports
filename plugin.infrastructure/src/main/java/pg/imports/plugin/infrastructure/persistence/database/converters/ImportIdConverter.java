package pg.imports.plugin.infrastructure.persistence.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pg.imports.plugin.api.data.ImportId;

@Converter(autoApply = true)
public class ImportIdConverter implements AttributeConverter<ImportId, String> {

    @Override
    public String convertToDatabaseColumn(final ImportId importId) {
        return importId != null ? importId.id() : null;
    }

    @Override
    public ImportId convertToEntityAttribute(final String dbValue) {
        return dbValue != null ? new ImportId(dbValue) : null;
    }
}

