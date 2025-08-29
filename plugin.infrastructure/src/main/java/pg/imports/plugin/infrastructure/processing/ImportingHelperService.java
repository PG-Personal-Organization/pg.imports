package pg.imports.plugin.infrastructure.processing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;
import pg.context.auth.api.context.provider.ContextProvider;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.*;
import pg.imports.plugin.api.service.ImportingHelper;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportFactory;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.imports.plugin.infrastructure.plugins.ImportPluginNotFoundException;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.errors.ImportFileNotFoundException;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportImportingEvent;
import pg.imports.plugin.infrastructure.processing.events.ScheduledImportParsingEvent;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.service.api.FileService;
import pg.lib.awsfiles.service.api.FileView;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class ImportingHelperService implements ImportingHelper {
    private final ContextProvider contextProvider;
    private final PluginCache pluginCache;
    private final ImportRepository importRepository;
    private final RecordsRepository recordsRepository;
    private final FileService fileService;
    private final EventSender eventSender;

    @Override
    @Transactional
    public ImportId scheduleImport(final String pluginCode, final UUID fileId) {
        var code = new PluginCode(pluginCode);
        var importPlugin = pluginCache.tryGetPlugin(code)
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", code)));
        var importId = startImport(importPlugin, fileId);
        log.info("Import scheduled with id {}", importId);
        return importId;
    }

    @Override
    @Transactional
    public void confirmImporting(final ImportId importId) {
        var userContext = contextProvider.tryToGetUserContext().orElseThrow(() -> new IllegalStateException("No user context found"));
        var importEntity = importRepository.getParsedImport(importId.id());

        if (!Objects.equals(importEntity.getUserId(), userContext.getUserId())) {
            throw new IllegalStateException(String.format("User %s is not allowed to confirm importing of import %s", userContext.getUserId(), importId));
        }

        log.info("Import {} importing triggered", importEntity);
        eventSender.sendEvent(ScheduledImportImportingEvent.of(importId));
    }

    @Override
    public ImportData findImportStatus(final @NonNull String importId, final @NonNull List<ImportStatus> statuses) {
        return importRepository.findByIdAndStatusIn(
                        importId,
                        statuses.stream().map(ImportStatus::name).map(pg.imports.plugin.infrastructure.persistence.database.imports.ImportStatus::valueOf).toList())
                .map(this::toImportData)
                .orElse(null);
    }

    @Override
    public ImportRecordsData getImportRecords(final @NonNull String importId) {
        var partitions = recordsRepository.findAllByParent_Id(importId);
        return ImportRecordsData.builder()
                .partitions(partitions.stream().map(this::toImportRecordsPartition).toList())
                .build();
    }

    private ImportId startImport(final ImportPlugin importPlugin, final UUID fileId) {
        Optional<FileView> contentFile = fileService.findById(fileId);

        if (contentFile.isEmpty()) {
            throw new ImportFileNotFoundException(String.format("File with id %s not found", fileId));
        }

        var userContext = contextProvider.tryToGetUserContext().orElseThrow(() -> new IllegalStateException("No user context found"));
        var importEntity = ImportFactory.createScheduledImport(importPlugin, fileId, userContext.getUserId());
        importRepository.save(importEntity);
        var importId = new ImportId(importEntity.getId());

        eventSender.sendEvent(ScheduledImportParsingEvent.of(importId));
        return importId;
    }

    private ImportData toImportData(final ImportEntity importEntity) {
        return ImportData.builder()
                .id(importEntity.getId())
                .createdOn(importEntity.getCreatedOn())
                .startedParsingOn(importEntity.getStartedParsingOn())
                .endedParsingOn(importEntity.getEndedParsingOn())
                .startedImportingOn(importEntity.getStartedImportingOn())
                .finishedImportingOn(importEntity.getFinishedImportingOn())
                .status(ImportStatus.valueOf(importEntity.getStatus().name()))
                .countOfRecordPartitions(importEntity.getRecords().size())
                .fileId(importEntity.getFileId())
                .pluginCode(importEntity.getPluginCode().code())
                .ownerId(importEntity.getUserId())
                .rejectedReason(importEntity.getRejectedReason())
                .build();
    }

    private ImportRecordsPartition toImportRecordsPartition(final ImportRecordsEntity records) {
        return ImportRecordsPartition.builder()
                .id(records.getId())
                .partitionNumber(records.getPartitionNumber())
                .finishedParsingOn(records.getFinishedParsingOn())
                .startedImportingOn(records.getStartedImportingOn())
                .finishedImportingOn(records.getFinishedImportingOn())
                .recordsStatus(RecordsStatus.valueOf(records.getRecordsStatus().name()))
                .successRecordIds(records.getRecordIds())
                .errorRecordIds(records.getErrorRecordIds())
                .count(records.getCount())
                .errorCount(records.getErrorCount())
                .errorMessages(records.getErrorMessages())
                .importId(records.getParent().getId())
                .strategy(records.getStrategy())
                .build();
    }
}
