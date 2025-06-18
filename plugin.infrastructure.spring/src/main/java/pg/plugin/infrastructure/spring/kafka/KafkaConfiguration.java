package pg.plugin.infrastructure.spring.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pg.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.plugin.infrastructure.parsing.RejectedImportParsingMessageHandler;
import pg.plugin.infrastructure.parsing.ScheduledImportsMessageHandler;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.plugin.infrastructure.plugins.PluginCache;

@Configuration
public class KafkaConfiguration {

    @Bean
    public ScheduledImportsMessageHandler scheduledImportsMessageHandler(final ImportRepository importRepository, final ParsingJobLauncher parsingJobLauncher,
                                                                         final PluginCache pluginCache) {
        return new ScheduledImportsMessageHandler(importRepository, parsingJobLauncher, pluginCache);
    }

    @Bean
    public RejectedImportParsingMessageHandler rejectedImportsMessageHandler(final ImportRepository importRepository, final PluginCache pluginCache) {
        return new RejectedImportParsingMessageHandler(importRepository, pluginCache);
    }
}
