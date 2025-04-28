package pg.plugin.infrastructure.spring.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.plugin.infrastructure.spring.common.config.ImportsConfig;
import pg.plugin.infrastructure.spring.common.config.ImportsConfigProvider;
import pg.plugin.infrastructure.spring.kafka.KafkaConfiguration;
import pg.plugin.infrastructure.spring.persistence.DatabaseConfiguration;

@ConditionalOnProperty(value = "pg.imports.enabled", havingValue = "true")
@Import({
        BatchConfiguration.class,
        KafkaConfiguration.class,
        DatabaseConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportPluginConfiguration {
    private final ImportsConfig importsConfig;

    @Bean
    public ImportsConfigProvider importsConfigProvider() {
        return new ImportsConfigProvider(importsConfig);
    }
}
