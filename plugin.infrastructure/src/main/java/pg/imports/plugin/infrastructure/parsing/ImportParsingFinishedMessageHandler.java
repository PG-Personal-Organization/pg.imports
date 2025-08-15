package pg.imports.plugin.infrastructure.parsing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.processing.events.ImportParsingFinishedEvent;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;
import pg.kafka.consumer.MessageHandler;
import pg.kafka.sender.EventSender;

@Log4j2
@RequiredArgsConstructor
public class ImportParsingFinishedMessageHandler implements MessageHandler<ImportParsingFinishedEvent> {
    private final ImportRepository importRepository;
    private final EventSender eventSender;

    @Override
    public void handleMessage(final @NonNull ImportParsingFinishedEvent message) {
        ImportEntity parsedImport = importRepository.getParsedImport(message.getId());

        log.info("Import {} importing triggered", parsedImport);
        eventSender.sendEvent(ScheduledImportImportingEvent.of(parsedImport.getImportId()));
    }

    @Override
    public Class<ImportParsingFinishedEvent> getMessageType() {
        return ImportParsingFinishedEvent.class;
    }
}
