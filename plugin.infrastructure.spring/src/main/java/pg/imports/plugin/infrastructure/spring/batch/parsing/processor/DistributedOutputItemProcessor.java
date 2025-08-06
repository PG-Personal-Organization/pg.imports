package pg.imports.plugin.infrastructure.spring.batch.parsing.processor;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import pg.imports.plugin.infrastructure.plugins.PluginCache;

@Log4j2
public class DistributedOutputItemProcessor extends ReaderOutputItemProcessor {

    public DistributedOutputItemProcessor(final StepExecution stepExecution, final PluginCache pluginCache) {
        super(stepExecution, pluginCache);
    }
}
