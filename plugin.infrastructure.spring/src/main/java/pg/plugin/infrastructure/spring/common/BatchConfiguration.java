package pg.plugin.infrastructure.spring.common;

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

}
