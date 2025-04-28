package pg.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.consumer.MessageHandler;
import pg.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.persistence.imports.ImportStatus;
import pg.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.errors.ScheduledImportNotExistException;
import pg.plugin.infrastructure.processing.events.ScheduledImportEvent;

@Log4j2
@RequiredArgsConstructor
public class ScheduledImportsMessageHandler implements MessageHandler<ScheduledImportEvent> {
    private final ImportRepository importRepository;
    private final ParsingJobLauncher parsingJobLauncher;
    private final PluginCache pluginCache;

    @Override
    @Transactional
    public void handleMessage(final @NonNull ScheduledImportEvent message) {
        var importId = message.getImportId();
        ImportEntity newImport = importRepository.findByIdAndStatus(importId.id(), ImportStatus.NEW)
                .orElseThrow(() -> new ScheduledImportNotExistException(String.format("Import with id %s and status NEW not found", importId)));

        var plugin = pluginCache.tryGetPlugin(newImport.getPluginCode())
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", newImport.getPluginCode())));
        newImport.startParsing();

        importRepository.save(newImport);
        log.info("Import {} parsing started", newImport);
        parsingJobLauncher.launchParsingJob(plugin, newImport);
    }

    @Override
    public Class<ScheduledImportEvent> getMessageType() {
        return ScheduledImportEvent.class;
    }
}
