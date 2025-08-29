package pg.imports.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import pg.imports.plugin.api.parsing.RecordsParsingErrorHandler;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.ChunksHelper;
import pg.imports.plugin.infrastructure.processing.events.RejectImportParsingEvent;
import pg.kafka.consumer.MessageHandler;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class RejectedImportParsingMessageHandler implements MessageHandler<RejectImportParsingEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Value("${pg.imports.batch.cleaning.parts.size:200}")
    private int cleaningPartsSize;

    @Override
    public void handleMessage(final @NonNull RejectImportParsingEvent message) {
        var importId = message.getImportId();
        ImportEntity parsingImport = importRepository.getRejectedParsingImport(importId.id());

        var plugin = pluginCache.tryGetPlugin(parsingImport.getPluginCode())
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", parsingImport.getPluginCode())));

        try {
            if (message.getReason() != null && (message.getRecordIds() != null) && !message.getRecordIds().isEmpty()) {
                RecordsParsingErrorHandler parsingErrorHandler = plugin.getParsingComponentProvider().getRecordsParsingErrorHandler();
                clean(message.getRecordIds(), parsingErrorHandler);
            }
        } catch (final Exception e) {
            log.error("Error occurred during error handling in plugin, continuing rejection without plugin involvement.", e);
        }

        parsingImport.rejectParsing(message.getReason());
        importRepository.save(parsingImport);
        log.info("Import {} parsing finished with error: {}", parsingImport, message.getReason());
    }

    private void clean(final List<String> recordIds, final RecordsParsingErrorHandler recordsParsingErrorHandler) {
        final int chunkSize = Math.max(1, cleaningPartsSize);
        ChunksHelper.forEachChunk(recordIds, chunkSize, recordsParsingErrorHandler::handleError);
    }

    @Override
    public Class<RejectImportParsingEvent> getMessageType() {
        return RejectImportParsingEvent.class;
    }
}
