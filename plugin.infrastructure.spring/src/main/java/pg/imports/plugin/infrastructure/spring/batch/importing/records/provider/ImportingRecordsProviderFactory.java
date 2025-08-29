package pg.imports.plugin.infrastructure.spring.batch.importing.records.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.importing.ImportingRecordsProvider;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;
import pg.imports.plugin.infrastructure.persistence.database.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.database.records.db.RecordRepository;
import pg.imports.plugin.infrastructure.persistence.mongo.MongoRecordRepository;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ImportingRecordsProviderFactory {
    private final RecordRepository recordRepository;
    private final MongoRecordRepository mongoRecordRepository;
    private final ObjectMapper batchObjectMapper;

    public ImportingRecordsProvider resolveProvider(final RecordsStoringStrategy strategy, final ImportPlugin plugin, final List<ImportRecordsEntity> recordsPartitions) {
        var successfulRecordIds = recordsPartitions.stream().map(ImportRecordsEntity::getRecordIds).flatMap(Collection::stream).toList();
        return switch (strategy) {
            case PLUGIN_DATABASE -> plugin.getImportingComponentsProvider().getPluginImportingRecordsProvider(successfulRecordIds);
            case LIBRARY_JSON_DATABASE -> new LibraryJsonImportingRecordsProvider(recordRepository, batchObjectMapper, successfulRecordIds);
            case MONGO_REPOSITORY -> new MongoImportingRecordsProvider(mongoRecordRepository, batchObjectMapper, successfulRecordIds);
        };
    }
}
