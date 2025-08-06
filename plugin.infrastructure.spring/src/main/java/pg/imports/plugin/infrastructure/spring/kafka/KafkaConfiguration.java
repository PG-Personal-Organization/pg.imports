package pg.imports.plugin.infrastructure.spring.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.infrastructure.importing.CompletedImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.imports.plugin.infrastructure.importing.RejectImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.importing.ScheduledImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ImportParsingFinishedMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.imports.plugin.infrastructure.parsing.RejectedImportParsingMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ScheduledImportParsingMessageHandler;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;

@Import({
        pg.kafka.config.KafkaConfiguration.class,
})
@Configuration
public class KafkaConfiguration {

    @Bean
    public ScheduledImportParsingMessageHandler scheduledImportsMessageHandler(final ImportRepository importRepository,
                                                                               final ParsingJobLauncher parsingJobLauncher,
                                                                               final PluginCache pluginCache) {
        return new ScheduledImportParsingMessageHandler(importRepository, parsingJobLauncher, pluginCache);
    }

    @Bean
    public RejectedImportParsingMessageHandler rejectedImportsMessageHandler(final ImportRepository importRepository,
                                                                             final PluginCache pluginCache) {
        return new RejectedImportParsingMessageHandler(importRepository, pluginCache);
    }

    @Bean
    public ImportParsingFinishedMessageHandler importParsingFinishedMessageHandler(final ImportRepository importRepository,
                                                                                   final EventSender eventSender) {
        return new ImportParsingFinishedMessageHandler(importRepository, eventSender);
    }

    @Bean
    public ScheduledImportImportingMessageHandler scheduledImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                         final ImportingJobLauncher importingJobLauncher,
                                                                                         final PluginCache pluginCache) {
        return new ScheduledImportImportingMessageHandler(importRepository, importingJobLauncher, pluginCache);
    }

    @Bean
    public CompletedImportImportingMessageHandler completedImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                         final PluginCache pluginCache) {
        return new CompletedImportImportingMessageHandler(importRepository, pluginCache);
    }

    @Bean
    public RejectImportImportingMessageHandler rejectImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                   final PluginCache pluginCache) {
        return new RejectImportImportingMessageHandler(importRepository, pluginCache);
    }
}
