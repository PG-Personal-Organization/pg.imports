package pg.plugin.infrastructure.spring.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.lib.awsfiles.config.AmazonConfig;
import pg.plugin.infrastructure.spring.batch.ImportingConfiguration;
import pg.plugin.infrastructure.spring.batch.ParsingConfiguration;

@Import({
        ImportingConfiguration.class,
        ParsingConfiguration.class,
        AmazonConfig.class
})
@Configuration
public class BatchConfiguration {
}
