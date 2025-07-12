package pg.plugin.infrastructure.processing.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pg.kafka.message.Message;
import pg.plugin.api.data.ImportId;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class ScheduledImportParsingEvent extends Message {
    private final ImportId importId;
}
