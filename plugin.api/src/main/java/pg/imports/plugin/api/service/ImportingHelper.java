package pg.imports.plugin.api.service;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportData;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.ImportRecordsData;
import pg.imports.plugin.api.data.ImportStatus;

import java.util.List;
import java.util.UUID;

public interface ImportingHelper {
    ImportId scheduleImport(String pluginCode, UUID fileId);

    void confirmImporting(ImportId importId);

    ImportData findImportStatus(@NonNull String importId, @NonNull List<ImportStatus> statuses);

    ImportRecordsData getImportRecords(@NonNull String importId);
}
