package pg.plugin.infrastructure.spring.common.config;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import pg.plugin.api.data.PluginCode;
import pg.plugin.api.strategies.RecordsStoringStrategy;

import java.util.Optional;

@AllArgsConstructor
public class ImportsConfigProvider {
    private ImportsConfig importsConfig;

    public ImportStrategy getParsingStrategy(final @NonNull PluginCode pluginCode) {
        return importsConfig.getPluginsParsingStrategy().getOrDefault(
                pluginCode.code(),
                Optional.ofNullable(importsConfig.getParsingStrategy()).orElse(ImportStrategy.LOCAL)
        );
    }

    public ImportStrategy getImportingStrategy(final @NonNull PluginCode pluginCode) {
        return importsConfig.getPluginsImportStrategy().getOrDefault(
          pluginCode.code(),
          Optional.ofNullable(importsConfig.getImportStrategy()).orElse(ImportStrategy.LOCAL)
        );
    }

    public KafkaImportsMessageStrategy getKafkaMessage(final @NonNull PluginCode pluginCode) {
        return importsConfig.getPluginsKafkaImportsMessage().getOrDefault(
                pluginCode.code(),
                Optional.ofNullable(importsConfig.getKafkaImportsMessageStrategy()).orElse(KafkaImportsMessageStrategy.LIGHT_RECORDS)
        );
    }

    public RecordsStoringStrategy getRecordsStoring(final @NonNull PluginCode pluginCode) {
        return importsConfig.getPluginsDatabaseRecordsStoring().getOrDefault(
                pluginCode.code(),
                Optional.ofNullable(importsConfig.getRecordsStoringStrategy()).orElse(RecordsStoringStrategy.LIBRARY_JSON_DATABASE)
        );
    }
}
