package pg.imports.plugin.api.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@ToString
@Getter
@RequiredArgsConstructor(staticName = "of")
public class ImportContext implements Serializable {
    private final ImportId importId;

    private final PluginCode pluginCode;

    private final UUID fileId;
}
