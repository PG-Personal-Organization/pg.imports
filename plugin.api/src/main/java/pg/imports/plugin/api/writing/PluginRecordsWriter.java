package pg.imports.plugin.api.writing;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.data.ImportRecordStatus;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static pg.imports.plugin.api.data.ImportRecordStatus.*;

public interface PluginRecordsWriter<RECORD extends RecordData, PARSED_RECORD extends ParsedRecord<RECORD>> {

    Set<ImportRecordStatus> ERROR_STATUSES = Set.of(PARSING_FAILED, IMPORTING_FAILED);
    Set<ImportRecordStatus> SUCCESS_STATUSES = Set.of(PARSED, IMPORTED);

    @NonNull
    default WrittenRecords writeRecords(List<PARSED_RECORD> records, ImportContext importContext) {
        write(records, importContext);

        var recordsByStatus = records.stream()
                .collect(Collectors.groupingBy(
                        PARSED_RECORD::getRecordStatus,
                        Collectors.mapping(r -> r, Collectors.toList())
                ));

        var errorMessages = recordsByStatus.entrySet().stream()
                .filter(entry -> ERROR_STATUSES.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toMap(PARSED_RECORD::getRecordId,
                        r -> r.getErrorMessages().stream().reduce((s1, s2) -> s1.concat("\n").concat(s2)).orElse(""),
                        (l, r) -> l));

        return new WrittenRecords(getRecordIdsByStatus(recordsByStatus, SUCCESS_STATUSES), getRecordIdsByStatus(recordsByStatus, ERROR_STATUSES), errorMessages);
    }

    private List<String> getRecordIdsByStatus(final Map<ImportRecordStatus, List<PARSED_RECORD>> records, final Set<ImportRecordStatus> statuses) {
        return records.entrySet().stream()
                .filter(entry -> statuses.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .map(PARSED_RECORD::getRecordId)
                .toList();
    }

    void write(List<PARSED_RECORD> records, ImportContext importContext);

    void writeRecordError(String recordId, String errorMessage);
}
