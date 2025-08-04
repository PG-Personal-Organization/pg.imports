package pg.plugin.infrastructure.processing.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pg.kafka.message.Message;
import pg.plugin.api.data.ImportId;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RequiredArgsConstructor(staticName = "of")
public class RejectImportParsingEvent extends Message {
    private final ImportId importId;
    private final String reason;
    private List<String> recordIds;

    public static RejectImportParsingEvent of(final ImportId importId, final String reason, final List<String> recordIds) {
        return new RejectImportParsingEvent(importId, reason, recordIds);
    }

    @SuppressWarnings("checkstyle:HiddenField")
    private RejectImportParsingEvent(final ImportId importId, final String reason, final List<String> recordIds) {
        this(importId, reason);
        this.recordIds = recordIds;
    }
}
