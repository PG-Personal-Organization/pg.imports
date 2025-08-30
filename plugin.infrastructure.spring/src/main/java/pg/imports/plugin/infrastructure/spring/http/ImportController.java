package pg.imports.plugin.infrastructure.spring.http;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;
import pg.imports.plugin.api.data.*;
import pg.imports.plugin.api.service.ImportingHelper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ImportsHttpPaths.BASE_PATH)
@AllArgsConstructor
@Tag(name = "Imports")
public class ImportController {
    private final ImportingHelper importingHelper;

    @PostMapping(value = "/start/{fileId}/{pluginCode}")
    public ImportId startImporting(final @NonNull @PathVariable("fileId") UUID fileId, final @NonNull @PathVariable("pluginCode") String pluginCode) {
        return importingHelper.scheduleImport(pluginCode, fileId);
    }

    @PostMapping(value = "/confirm/{importId}")
    public void confirmImporting(final @NonNull @PathVariable("importId") String importId) {
        importingHelper.confirmImporting(new ImportId(importId));
    }

    @GetMapping(value = "/status/{importId}/{statuses}")
    public ImportDataResponse getImportStatus(final @NonNull @PathVariable("importId") String importId, final @NonNull @PathVariable("statuses") List<ImportStatus> statuses) {
        return ImportDataResponse.builder().data(importingHelper.findImportStatus(importId, statuses)).build();
    }

    @GetMapping(value = "/records/{importId}")
    public ImportRecordsData getImportRecords(final @NonNull @PathVariable("importId") String importId) {
        return importingHelper.getImportRecords(importId);
    }
}
