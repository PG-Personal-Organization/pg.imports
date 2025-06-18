package pg.plugin.api.records.writing;

import lombok.NonNull;

import java.util.List;
import java.util.Map;

public record WrittenRecords(@NonNull List<String> recordIds, @NonNull List<String> errorRecordIds, @NonNull Map<String, String> errorMessages) {
}
