package pg.plugin.api.parsing;

import org.beanio.builder.StreamBuilder;

public interface ReaderDefinition {
    String getReaderName();

    StreamBuilder getStreamBuilder();
}
