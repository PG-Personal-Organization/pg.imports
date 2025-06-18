package pg.plugin.infrastructure.spring.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.kafka.sender.EventSender;
import pg.lib.awsfiles.infrastructure.config.AmazonConfig;
import pg.lib.awsfiles.service.api.FileService;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.service.ImportingHelper;
import pg.plugin.infrastructure.plugins.PluginCache;
import pg.plugin.infrastructure.processing.ImportingHelperService;
import pg.plugin.infrastructure.spring.batch.ImportingConfiguration;
import pg.plugin.infrastructure.spring.batch.ParsingConfiguration;

import java.util.List;

@Import({
        ImportingConfiguration.class,
        ParsingConfiguration.class,
        AmazonConfig.class
})
@Configuration
public class BatchConfiguration {

    @Bean
    public PluginCache pluginCache(final List<ImportPlugin> importPlugins) {
        return new PluginCache(importPlugins);
    }

    @Bean
    public ImportingHelper importingHelper(final PluginCache pluginCache, final FileService fileService, final EventSender eventSender) {
        return new ImportingHelperService(pluginCache, fileService, eventSender);
    }

    @Bean
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

}
