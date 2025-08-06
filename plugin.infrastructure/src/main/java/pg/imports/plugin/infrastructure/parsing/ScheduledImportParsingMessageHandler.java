package pg.imports.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.consumer.MessageHandler;
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportParsingEvent;

@Log4j2
@RequiredArgsConstructor
public class ScheduledImportParsingMessageHandler implements MessageHandler<ScheduledImportParsingEvent> {
    private final ImportRepository importRepository;
    private final ParsingJobLauncher parsingJobLauncher;
    private final PluginCache pluginCache;

    @Override
    @Transactional
    public void handleMessage(final @NonNull ScheduledImportParsingEvent message) {
        var importId = message.getImportId();
        ImportEntity newImport = importRepository.getNewImport(importId.id());

        var plugin = pluginCache.tryGetPlugin(newImport.getPluginCode())
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", newImport.getPluginCode())));
        newImport.startParsing();

        importRepository.save(newImport);
        log.info("Import {} parsing started", newImport);
        parsingJobLauncher.launchParsingJob(plugin, newImport);
    }

    @Override
    public Class<ScheduledImportParsingEvent> getMessageType() {
        return ScheduledImportParsingEvent.class;
    }
}
