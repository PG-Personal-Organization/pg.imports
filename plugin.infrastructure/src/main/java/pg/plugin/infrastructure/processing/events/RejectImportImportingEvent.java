package pg.plugin.infrastructure.processing.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pg.kafka.message.Message;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.data.PluginCode;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RequiredArgsConstructor(staticName = "of")
public class RejectImportImportingEvent extends Message {
    private final ImportId importId;
    private final PluginCode pluginCode;
    private final String reason;
    private final List<String> recordIds;
}
