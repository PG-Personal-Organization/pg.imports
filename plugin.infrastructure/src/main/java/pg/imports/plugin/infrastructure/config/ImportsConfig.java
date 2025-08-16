package pg.imports.plugin.infrastructure.config;

import pg.imports.plugin.api.strategies.RecordsStoringStrategy;

import java.util.Map;

public interface ImportsConfig {
    boolean isAutoStartImportingEnabled();

    KafkaImportsMessageStrategy getKafkaImportsMessageStrategy();

    RecordsStoringStrategy getRecordsStoringStrategy();

    ImportStrategy getImportStrategy();

    ImportStrategy getParsingStrategy();

    Map<String, KafkaImportsMessageStrategy> getPluginsKafkaImportsMessage();

    Map<String, RecordsStoringStrategy> getPluginsDatabaseRecordsStoring();

    Map<String, ImportStrategy> getPluginsParsingStrategy();

    Map<String, ImportStrategy> getPluginsImportStrategy();
}
