package pg.plugin.infrastructure.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.consumer.MessageHandler;
import pg.plugin.api.data.PluginCode;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.events.RejectImportImportingEvent;

@RequiredArgsConstructor
@Log4j2
public class RejectImportImportingMessageHandler implements MessageHandler<RejectImportImportingEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Override
    @Transactional
    public void handleMessage(final @NonNull RejectImportImportingEvent message) {
        var importId = message.getImportId();
        log.info("Rejecting importing of import {} with reason: {}", importId, message.getReason());

        var importEntity = importRepository.getImportingImport(importId.id());
        importEntity.rejectImporting(message.getReason());
        importRepository.save(importEntity);
        log.info("Saving rejected importing of import {} with reason: {}", importId, message.getReason());

        try {
            PluginCode pluginCode = message.getPluginCode();
            log.info("Handling errors cleaning in plugin: {} for import {}", pluginCode, importId);
            var plugin = pluginCache.getPlugin(pluginCode);
            var errorHandler = plugin.getImportingComponentsProvider().getRecordsImportingErrorHandler();
            errorHandler.handleImportingError(message.getRecordIds());
        } catch (final Exception e) {
            log.error("Error occurred during error handling in plugin, continuing without plugin involvement.", e);
        }
    }

    @Override
    public Class<RejectImportImportingEvent> getMessageType() {
        return RejectImportImportingEvent.class;
    }
}
