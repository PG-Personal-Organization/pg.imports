package pg.plugin.infrastructure.spring.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pg.plugin.api.strategies.RecordsStoringStrategy;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "pg.imports")
@Configuration
@Data
public class ImportsConfig {
    private KafkaImportsMessageStrategy kafkaImportsMessageStrategy;

    private RecordsStoringStrategy recordsStoringStrategy;

    private ImportStrategy importStrategy;

    private ImportStrategy parsingStrategy;

    private Map</*PluginCode*/ String, KafkaImportsMessageStrategy> pluginsKafkaImportsMessage = new HashMap<>();

    private Map</*PluginCode*/ String, RecordsStoringStrategy> pluginsDatabaseRecordsStoring = new HashMap<>();

    private Map</*PluginCode*/ String, ImportStrategy> pluginsImportStrategy = new HashMap<>();

    private Map</*PluginCode*/ String, ImportStrategy> pluginsParsingStrategy = new HashMap<>();
}
