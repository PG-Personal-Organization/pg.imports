package pg.imports.plugin.infrastructure.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.service.api.FileService;
import pg.lib.awsfiles.service.api.FileView;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.service.ImportingHelper;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.infrastructure.persistence.imports.ImportFactory;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.errors.ImportFileNotFoundException;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportParsingEvent;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class ImportingHelperService implements ImportingHelper {
    private final PluginCache pluginCache;
    private final ImportRepository importRepository;
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

    @Override
    @Transactional
    public void confirmImporting(final ImportId importId) {
        var importEntity = importRepository.getParsedImport(importId.id());
        log.info("Import {} importing triggered", importEntity);
        eventSender.sendEvent(ScheduledImportImportingEvent.of(importId));
    }

    private ImportId startImport(final ImportPlugin importPlugin, final UUID fileId) {
        Optional<FileView> contentFile = fileService.findById(fileId);

        if (contentFile.isEmpty()) {
            throw new ImportFileNotFoundException(String.format("File with id %s not found", fileId));
        }

//        Optional<String> contextToken = headersHolder.tryToGetHeader(HeaderNames.CONTEXT_TOKEN);
        var importEntity = ImportFactory.createScheduledImport(importPlugin, fileId, null);
        importRepository.save(importEntity);
        var importId = new ImportId(importEntity.getId());

        eventSender.sendEvent(ScheduledImportParsingEvent.of(importId));
        return importId;
    }


}
