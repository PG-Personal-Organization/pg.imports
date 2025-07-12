
package pg.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.consumer.MessageHandler;
import pg.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;

@Log4j2
@RequiredArgsConstructor
public class ScheduledImportImportingMessageHandler implements MessageHandler<ScheduledImportImportingEvent> {
    private final ImportRepository importRepository;
    private final ImportingJobLauncher importingJobLauncher;
    private final PluginCache pluginCache;

    @Override
    @Transactional
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
