package pg.imports.plugin.infrastructure.spring.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import pg.imports.plugin.infrastructure.config.ImportsConfigProvider;
import pg.imports.plugin.infrastructure.processing.events.*;
import pg.kafka.message.MessageDestination;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.infrastructure.importing.CompletedImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.importing.ImportingJobLauncher;
import pg.imports.plugin.infrastructure.importing.RejectImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.importing.ScheduledImportImportingMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ImportParsingFinishedMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ParsingJobLauncher;
import pg.imports.plugin.infrastructure.parsing.RejectedImportParsingMessageHandler;
import pg.imports.plugin.infrastructure.parsing.ScheduledImportParsingMessageHandler;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.kafka.topic.TopicDefinition;
import pg.kafka.topic.TopicName;

@Import({
        pg.kafka.config.KafkaConfiguration.class,
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KafkaConfiguration {
    private final Environment environment;

    @Bean
    public TopicDefinition scheduledImportsParsingTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-scheduled-imports-parsing-topic"))
                .build();
    }

    @Bean
    public MessageDestination scheduledImportsParsingMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-scheduled-imports-parsing-topic"))
                .messageClass(ScheduledImportParsingEvent.class)
                .build();
    }

    @Bean
    public ScheduledImportParsingMessageHandler scheduledImportsMessageHandler(final ImportRepository importRepository,
                                                                               final ParsingJobLauncher parsingJobLauncher,
                                                                               final PluginCache pluginCache) {
        return new ScheduledImportParsingMessageHandler(importRepository, parsingJobLauncher, pluginCache);
    }

    @Bean
    public TopicDefinition rejectedImportsParsingTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-rejected-imports-parsing-topic"))
                .build();
    }

    @Bean
    public MessageDestination rejectedImportsParsingMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-rejected-imports-parsing-topic"))
                .messageClass(RejectImportParsingEvent.class)
                .build();
    }

    @Bean
    public RejectedImportParsingMessageHandler rejectedImportsMessageHandler(final ImportRepository importRepository,
                                                                             final PluginCache pluginCache) {
        return new RejectedImportParsingMessageHandler(importRepository, pluginCache);
    }

    @Bean
    public TopicDefinition importsParsingFinishedTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-imports-parsing-finished-topic"))
                .build();
    }

    @Bean
    public MessageDestination importsParsingFinishedMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-imports-parsing-finished-topic"))
                .messageClass(ImportParsingFinishedEvent.class)
                .build();
    }

    @Bean
    public ImportParsingFinishedMessageHandler importParsingFinishedMessageHandler(final ImportsConfigProvider importsConfigProvider,
                                                                                   final ImportRepository importRepository,
                                                                                   final EventSender eventSender) {
        return new ImportParsingFinishedMessageHandler(importsConfigProvider, importRepository, eventSender);
    }

    @Bean
    public TopicDefinition scheduledImportsParsingFinishedTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-scheduled-imports-importing-topic"))
                .build();
    }

    @Bean
    public MessageDestination scheduledImportsImportingMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-scheduled-imports-importing-topic"))
                .messageClass(ScheduledImportImportingEvent.class)
                .build();
    }

    @Bean
    public ScheduledImportImportingMessageHandler scheduledImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                         final ImportingJobLauncher importingJobLauncher,
                                                                                         final PluginCache pluginCache) {
        return new ScheduledImportImportingMessageHandler(importRepository, importingJobLauncher, pluginCache);
    }

    @Bean
    public TopicDefinition completedImportsImportingTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-completed-imports-importing-topic"))
                .build();
    }

    @Bean
    public MessageDestination completedImportImportingMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-completed-imports-importing-topic"))
                .messageClass(CompletedImportEvent.class)
                .build();
    }

    @Bean
    public CompletedImportImportingMessageHandler completedImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                         final PluginCache pluginCache) {
        return new CompletedImportImportingMessageHandler(importRepository, pluginCache);
    }

    @Bean
    public TopicDefinition rejectedImportImportingTopicDefinition() {
        var applicationName = getApplicationName();
        return TopicDefinition.DEFAULT
                .topic(TopicName.of(applicationName + "-rejected-imports-importing-topic"))
                .build();
    }

    @Bean
    public MessageDestination rejectedImportImportingMessageDestination() {
        var applicationName = getApplicationName();
        return MessageDestination.builder()
                .topic(TopicName.of(applicationName + "-rejected-imports-importing-topic"))
                .messageClass(RejectImportImportingEvent.class)
                .build();
    }

    @Bean
    public RejectImportImportingMessageHandler rejectImportImportingMessageHandler(final ImportRepository importRepository,
                                                                                   final PluginCache pluginCache) {
        return new RejectImportImportingMessageHandler(importRepository, pluginCache);
    }

    private String getApplicationName() {
        return environment.getProperty("spring.application.name");
    }
}
