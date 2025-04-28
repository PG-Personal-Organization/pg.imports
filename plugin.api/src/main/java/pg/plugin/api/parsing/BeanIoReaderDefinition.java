package pg.plugin.api.parsing;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.beanio.builder.StreamBuilder;

import java.nio.charset.Charset;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public abstract class BeanIoReaderDefinition implements ReaderDefinition {
    private String name;

    private StreamBuilder streamBuilder;

    private Charset charset;
}
