package pg.plugin.infrastructure.spring.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pg.plugin.api.strategies.RecordsStoring;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "pg.imports")
@Configuration
@Data
public class ImportsConfig {
    KafkaImportsMessage kafkaImportsMessage;

    RecordsStoring recordsStoring;

    ImportStrategy importStrategy;

    ImportStrategy parsingStrategy;

    Map</*PluginCode*/ String, KafkaImportsMessage> pluginsKafkaImportsMessage = new HashMap<>();

    Map</*PluginCode*/ String, RecordsStoring> pluginsDatabaseRecordsStoring = new HashMap<>();
}
