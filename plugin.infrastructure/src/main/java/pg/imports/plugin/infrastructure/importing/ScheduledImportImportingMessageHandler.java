
package pg.imports.plugin.infrastructure.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;
import pg.kafka.consumer.MessageHandler;

@Log4j2
@RequiredArgsConstructor
public class ScheduledImportImportingMessageHandler implements MessageHandler<ScheduledImportImportingEvent> {
    private final ImportRepository importRepository;
    private final ImportingJobLauncher importingJobLauncher;
    private final PluginCache pluginCache;

    @Override
    public void handleMessage(final @NonNull ScheduledImportImportingEvent message) {
        var importId = message.getImportId();
        ImportEntity parsedImport = importRepository.getParsedImport(importId.id());

        var plugin = pluginCache.tryGetPlugin(parsedImport.getPluginCode())
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", parsedImport.getPluginCode())));
        parsedImport.startImporting();

        importRepository.save(parsedImport);
        log.info("Import {} data importing started", parsedImport);
        importingJobLauncher.launchImportingJob(plugin, parsedImport);
    }

    @Override
    public Class<ScheduledImportImportingEvent> getMessageType() {
        return ScheduledImportImportingEvent.class;
    }
}
