package pg.imports.plugin.infrastructure.importing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import pg.imports.plugin.api.importing.CompletedImportingCleaner;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.ChunksHelper;
import pg.imports.plugin.infrastructure.processing.events.CompletedImportEvent;
import pg.kafka.consumer.MessageHandler;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class CompletedImportImportingMessageHandler implements MessageHandler<CompletedImportEvent> {
    private final ImportRepository importRepository;
    private final PluginCache pluginCache;

    @Value("${pg.imports.batch.cleaning.parts.size:200}")
    private int cleaningPartsSize;

    @Override
    @Transactional(readOnly = true)
    public void handleMessage(final @NonNull CompletedImportEvent message) {
        var importId = message.getImportId();
        log.info("Import {} cleaning triggered", importId);
        var completedImport = importRepository.getCompletedImport(importId.id());

        var plugin = pluginCache.getPlugin(completedImport.getPluginCode());
        var completedImportingCleaner = plugin.getImportingComponentsProvider().getCompletedImportingCleaner();

        var recordIds = completedImport.getRecords().stream().flatMap(r -> r.getRecordIds().stream()).toList();
        var errorRecordIds = completedImport.getRecords().stream().flatMap(r -> r.getErrorRecordIds().stream()).toList();

        clean(recordIds, errorRecordIds, completedImportingCleaner);
        log.debug("Import {} cleaning finished for imported records: {} and not imported: {}", completedImport.getImportId(), recordIds, errorRecordIds);
    }

    private void clean(final List<String> recordIds, final List<String> errorRecordIds, final CompletedImportingCleaner completedImportingCleaner) {
        final int chunkSize = Math.max(1, cleaningPartsSize);
        ChunksHelper.forEachChunk(recordIds, chunkSize, completedImportingCleaner::handleCleaningSuccessfulRecords);
        ChunksHelper.forEachChunk(errorRecordIds, chunkSize, completedImportingCleaner::handleCleaningFailedRecords);
    }

    @Override
    public Class<CompletedImportEvent> getMessageType() {
        return CompletedImportEvent.class;
    }
}
