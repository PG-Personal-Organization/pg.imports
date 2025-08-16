package pg.imports.plugin.infrastructure.spring.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import pg.imports.plugin.api.service.ImportingHelper;
import pg.imports.plugin.infrastructure.spring.common.config.ImportsConfigImpl;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.spring.http.ImportController;
import pg.imports.plugin.infrastructure.spring.kafka.KafkaConfiguration;
import pg.imports.plugin.infrastructure.spring.mongo.MongoConfiguration;
import pg.imports.plugin.infrastructure.spring.persistence.DatabaseConfiguration;

@ConditionalOnProperty(value = "pg.imports.enabled", havingValue = "true")
@Import({
        KafkaConfiguration.class,
        BatchConfiguration.class,
        DatabaseConfiguration.class,
        MongoConfiguration.class,
        ImportsConfigImpl.class
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportPluginConfiguration {
    private final ImportsConfigImpl importsConfig;

    @Bean
    public ImportsConfigProvider importsConfigProvider() {
        return new ImportsConfigProvider(importsConfig);
    }

    @Bean
    @Profile("USER")
    public ImportController importController(final ImportingHelper importingHelper) {
        return new ImportController(importingHelper);
    }
}
