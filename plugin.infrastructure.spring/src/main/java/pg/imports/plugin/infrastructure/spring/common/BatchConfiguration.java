package pg.imports.plugin.infrastructure.spring.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import pg.context.auth.api.context.provider.ContextProvider;
import pg.imports.plugin.infrastructure.persistence.database.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.database.records.RecordsRepository;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.infrastructure.config.AmazonConfiguration;
import pg.lib.awsfiles.service.api.FileService;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.service.ImportingHelper;
import pg.imports.plugin.infrastructure.plugins.PluginCache;
import pg.imports.plugin.infrastructure.processing.ImportingHelperService;
import pg.imports.plugin.infrastructure.spring.batch.importing.ImportingConfiguration;
import pg.imports.plugin.infrastructure.spring.batch.parsing.ParsingConfiguration;

import java.util.List;

@Import({
        ImportingConfiguration.class,
        ParsingConfiguration.class,
        AmazonConfiguration.class
})
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@EnableIntegration
public class BatchConfiguration {

    @Bean
    public PluginCache pluginCache(final List<ImportPlugin> importPlugins) {
        return new PluginCache(importPlugins);
    }

    @Bean
    public ImportingHelper importingHelper(final ContextProvider contextProvider,
                                           final PluginCache pluginCache,
                                           final ImportRepository importRepository,
                                           final RecordsRepository recordsRepository,
                                           final FileService fileService,
                                           final EventSender eventSender) {
        return new ImportingHelperService(contextProvider, pluginCache, importRepository, recordsRepository, fileService, eventSender);
    }

    @Bean(name = "batchObjectMapper")
    public ObjectMapper batchObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


}
