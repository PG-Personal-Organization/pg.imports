package pg.imports.plugin.infrastructure.spring.batch.parsing.processor;

import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.infrastructure.plugins.PluginCache;

@Log4j2
public class DistributedOutputItemProcessor extends ReaderOutputItemProcessor {

    public DistributedOutputItemProcessor(final PluginCache pluginCache) {
        super(null, pluginCache);
    }
}
