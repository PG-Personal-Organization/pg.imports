package pg.plugin.infrastructure.spring.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.plugin.infrastructure.spring.batch.importing.BatchDistributedImportingConfiguration;
import pg.plugin.infrastructure.spring.batch.importing.BatchDistributedParallelImportingConfiguration;
import pg.plugin.infrastructure.spring.batch.importing.BatchLocalImportingConfiguration;
import pg.plugin.infrastructure.spring.batch.importing.BatchParallelImportingConfiguration;

@Import({
        BatchLocalImportingConfiguration.class,
        BatchParallelImportingConfiguration.class,
        BatchDistributedImportingConfiguration.class,
        BatchDistributedParallelImportingConfiguration.class
})
@Configuration
public class ImportingConfiguration {
}
