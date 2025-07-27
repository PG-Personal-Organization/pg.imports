package pg.plugin.infrastructure.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.kafka.consumer.MessageHandler;
import pg.plugin.api.ImportPlugin;
import pg.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.events.CompletedImportEvent;

@Log4j2
@RequiredArgsConstructor
public class CompletedImportImportingMessageHandler implements MessageHandler<CompletedImportEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Override
    public void handleMessage(final @NonNull CompletedImportEvent message) {
        ImportEntity completedImport = importRepository.getCompletedImport(message.getId());
        ImportPlugin plugin = pluginCache.getPlugin(completedImport.getPluginCode());
        var completedImportingCleaner = plugin.getImportingComponentsProvider().getCompletedImportingCleaner();
        var recordIds = completedImport.getRecords().stream().flatMap(r -> r.getRecordIds().stream()).toList();
        var errorRecordIds = completedImport.getRecords().stream().flatMap(r -> r.getErrorRecordIds().stream()).toList();
        completedImportingCleaner.handleCleaning(recordIds, errorRecordIds);
        log.info("Import {} cleaning finished for imported records: {} and not imported: {}", completedImport.getImportId(), recordIds, errorRecordIds);
    }

    @Override
    public Class<CompletedImportEvent> getMessageType() {
        return CompletedImportEvent.class;
    }
}
