package pg.imports.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.processing.events.ImportParsingFinishedEvent;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;

@Log4j2
@RequiredArgsConstructor
public class ImportParsingFinishedMessageHandler implements MessageHandler<ImportParsingFinishedEvent> {
    private final ImportsConfigProvider importsConfigProvider;
    private final ImportRepository importRepository;
    private final EventSender eventSender;

    @Override
    public void handleMessage(final @NonNull ImportParsingFinishedEvent message) {
        var importId = message.getImportId();
        ImportEntity parsedImport = importRepository.getParsedImport(importId.id());

        if (importsConfigProvider.shouldAutoStartImporting()) {
            log.info("Import {} importing triggered", parsedImport);
            eventSender.sendEvent(ScheduledImportImportingEvent.of(importId));
        } else {
            log.info("Import {} importing not triggered, waiting for manual importing trigger", parsedImport);
        }
    }

    @Override
    public Class<ImportParsingFinishedEvent> getMessageType() {
        return ImportParsingFinishedEvent.class;
    }
}
