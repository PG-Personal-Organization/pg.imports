package pg.imports.plugin.infrastructure.persistence.imports;

import lombok.experimental.UtilityClass;
import pg.imports.plugin.api.ImportPlugin;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class ImportFactory {
    public ImportEntity createScheduledImport(final ImportPlugin importPlugin, final UUID fileId, final UUID userId) {
        return ImportEntity.builder()
                .id(generateId(importPlugin.getCodeIdPrefix(), importPlugin.getVersion()))
                .createdOn(LocalDateTime.now())
                .status(ImportStatus.NEW)
                .userId(userId)
                .fileId(fileId)
                .pluginCode(importPlugin.getCode().code())
                .build();
    }

    private String generateId(final String pluginCodePrefix, final String version) {
        return String.format("IMP_%S_%S_%S", pluginCodePrefix, version, UUID.randomUUID());
    }
}
