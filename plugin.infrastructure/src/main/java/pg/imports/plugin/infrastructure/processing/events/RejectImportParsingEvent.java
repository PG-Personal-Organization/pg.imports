package pg.imports.plugin.infrastructure.processing.events;

import lombok.*;
import pg.kafka.message.Message;
import pg.imports.plugin.api.data.ImportId;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class RejectImportParsingEvent extends Message {
    private ImportId importId;
    private String reason;
    private List<String> recordIds;

    public static RejectImportParsingEvent of(final ImportId importId, final String reason, final List<String> recordIds) {
        return new RejectImportParsingEvent(importId, reason, recordIds);
    }

    public static RejectImportParsingEvent of(final ImportId importId, final String reason) {
        return new RejectImportParsingEvent(importId, reason);
    }

    @SuppressWarnings("checkstyle:HiddenField")
    private RejectImportParsingEvent(final ImportId importId, final String reason, final List<String> recordIds) {
        this(importId, reason);
        this.recordIds = recordIds;
    }

    @SuppressWarnings("checkstyle:HiddenField")
    private RejectImportParsingEvent(final ImportId importId, final String reason) {
        this.importId = importId;
        this.reason = reason;
    }
}
