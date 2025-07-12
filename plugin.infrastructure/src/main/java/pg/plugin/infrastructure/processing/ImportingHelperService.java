package pg.plugin.infrastructure.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.service.api.FileService;
import pg.lib.awsfiles.service.api.FileView;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.service.ImportingHelper;
import pg.plugin.api.data.PluginCode;
import pg.plugin.infrastructure.persistence.imports.ImportFactory;
import pg.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.errors.ImportFileNotFoundException;
import pg.plugin.infrastructure.processing.events.ScheduledImportParsingEvent;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class ImportingHelperService implements ImportingHelper {
    private final PluginCache pluginCache;
    private final FileService fileService;
    private final EventSender eventSender;

    @Override
    @Transactional
    public ImportId scheduleImport(final String pluginCode, final UUID fileId) {
        var code = new PluginCode(pluginCode);
        var importPlugin = pluginCache.tryGetPlugin(code)
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", code)));
        var importId = startImport(importPlugin, fileId);
        log.info("Import scheduled with id {}", importId);
        return importId;
    }

    private ImportId startImport(final ImportPlugin importPlugin, final UUID fileId) {
        Optional<FileView> contentFile = fileService.findById(fileId);

        if (contentFile.isEmpty()) {
            throw new ImportFileNotFoundException(String.format("File with id %s not found", fileId));
        }

//        Optional<String> contextToken = headersHolder.tryToGetHeader(HeaderNames.CONTEXT_TOKEN);
        var importEntity = ImportFactory.createScheduledImport(importPlugin, fileId, null);
        var importId = new ImportId(importEntity.getId());

        eventSender.sendEvent(ScheduledImportParsingEvent.of(importId));
        return importId;
    }


}
