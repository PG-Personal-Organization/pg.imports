package pg.plugin.infrastructure.spring.delivery.http;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.service.ImportingHelper;

import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/imports")
@AllArgsConstructor
@Tag(name = "Imports")
public class ImportController {
    private final ImportingHelper importingHelper;

    @PostMapping(value = "/start/{fileId}/{pluginCode}")
    public ImportId startImporting(final @NonNull @PathVariable("fileId") UUID fileId, final @NonNull @PathVariable("pluginCode") String pluginCode) {
        return importingHelper.scheduleImport(pluginCode, fileId);
    }
}
