package pg.imports.plugin.api.data;

import lombok.*;
import pg.imports.plugin.api.strategies.RecordsStoringStrategy;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@ToString
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ImportContext implements Serializable {
    private ImportId importId;

    private PluginCode pluginCode;

    private UUID fileId;

    private RecordsStoringStrategy recordsStoringStrategy;
}
