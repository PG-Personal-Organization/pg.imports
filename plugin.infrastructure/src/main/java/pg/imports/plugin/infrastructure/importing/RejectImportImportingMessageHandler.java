package pg.imports.plugin.infrastructure.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import pg.imports.plugin.api.data.PluginCode;
import pg.imports.plugin.api.importing.RecordsImportingErrorHandler;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.ChunksHelper;
import pg.imports.plugin.infrastructure.processing.events.RejectImportImportingEvent;
import pg.kafka.consumer.MessageHandler;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
public class RejectImportImportingMessageHandler implements MessageHandler<RejectImportImportingEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Value("${pg.imports.batch.cleaning.parts.size:200}")
    private int cleaningPartsSize;

    @Override
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
            clean(message.getRecordIds(), errorHandler);
        } catch (final Exception e) {
            log.error("Error occurred during error handling in plugin, continuing without plugin involvement.", e);
        }
    }

    private void clean(final List<String> recordIds, final RecordsImportingErrorHandler errorHandler) {
        final int chunkSize = Math.max(1, cleaningPartsSize);
        ChunksHelper.forEachChunk(recordIds, chunkSize, errorHandler::handleImportingError);
    }

    @Override
    public Class<RejectImportImportingEvent> getMessageType() {
        return RejectImportImportingEvent.class;
    }
}
