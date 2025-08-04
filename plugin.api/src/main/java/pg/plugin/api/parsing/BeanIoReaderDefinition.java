package pg.plugin.api.parsing;

import lombok.*;
import org.beanio.builder.StreamBuilder;

import java.nio.charset.Charset;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class BeanIoReaderDefinition implements ReaderDefinition {
    private String name;

    private StreamBuilder streamBuilder;

    private Charset charset;

    @Override
    public @NonNull String getReaderName() {
        return name;
    }
}
