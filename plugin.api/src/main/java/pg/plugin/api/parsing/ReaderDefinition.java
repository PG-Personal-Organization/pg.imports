package pg.plugin.api.parsing;

import lombok.NonNull;
import org.beanio.builder.StreamBuilder;

public interface ReaderDefinition {
    @NonNull String getReaderName();

    @NonNull StreamBuilder getStreamBuilder();
}
