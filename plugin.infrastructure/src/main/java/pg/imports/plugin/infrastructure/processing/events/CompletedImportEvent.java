package pg.imports.plugin.infrastructure.processing.events;

import lombok.*;
import pg.kafka.message.Message;
import pg.imports.plugin.api.data.ImportId;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class CompletedImportEvent extends Message {
    private ImportId importId;
}
