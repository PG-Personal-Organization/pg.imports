package pg.imports.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.events.RejectImportParsingEvent;
import pg.kafka.consumer.MessageHandler;

@Log4j2
@RequiredArgsConstructor
public class RejectedImportParsingMessageHandler implements MessageHandler<RejectImportParsingEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Override
    public void handleMessage(final @NonNull RejectImportParsingEvent message) {
        var importId = message.getImportId();
        ImportEntity parsingImport = importRepository.getRejectedParsingImport(importId.id());

        var plugin = pluginCache.tryGetPlugin(parsingImport.getPluginCode())
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", parsingImport.getPluginCode())));

        try {
            if (message.getReason() != null && (message.getRecordIds() != null) && !message.getRecordIds().isEmpty()) {
                plugin.getParsingComponentProvider().getRecordsParsingErrorHandler().handleError(message.getRecordIds());
            }
        } catch (final Exception e) {
            log.error("Error occurred during error handling in plugin, continuing rejection without plugin involvement.", e);
        }

        parsingImport.rejectParsing(message.getReason());
        importRepository.save(parsingImport);
        log.info("Import {} parsing finished with error: {}", parsingImport, message.getReason());
    }

    @Override
    public Class<RejectImportParsingEvent> getMessageType() {
        return RejectImportParsingEvent.class;
    }
}
