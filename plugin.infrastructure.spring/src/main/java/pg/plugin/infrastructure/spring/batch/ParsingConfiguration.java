package pg.plugin.infrastructure.spring.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.plugin.infrastructure.spring.batch.parsing.BatchDistributedParallelParsingConfiguration;
import pg.plugin.infrastructure.spring.batch.parsing.BatchDistributedParsingConfiguration;
import pg.plugin.infrastructure.spring.batch.parsing.BatchLocalParsingConfiguration;
import pg.plugin.infrastructure.spring.batch.parsing.BatchParallelParsingConfiguration;

@Import({
        BatchLocalParsingConfiguration.class,
        BatchParallelParsingConfiguration.class,
        BatchDistributedParsingConfiguration.class,
        BatchDistributedParallelParsingConfiguration.class
})
@Configuration
public class ParsingConfiguration {
}
