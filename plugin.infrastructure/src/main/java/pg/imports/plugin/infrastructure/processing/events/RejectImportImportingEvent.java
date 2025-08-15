package pg.imports.plugin.infrastructure.processing.events;

import lombok.*;
import pg.kafka.message.Message;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.data.PluginCode;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RejectImportImportingEvent extends Message {
    private ImportId importId;
    private PluginCode pluginCode;
    private String reason;
    private List<String> recordIds;
}
