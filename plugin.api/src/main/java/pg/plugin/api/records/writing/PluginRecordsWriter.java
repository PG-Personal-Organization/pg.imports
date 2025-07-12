package pg.plugin.api.records.writing;

import lombok.NonNull;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.strategies.db.RecordData;

import java.util.List;

public interface PluginRecordsWriter<RECORD extends RecordData> {
    @NonNull
    WrittenRecords writeRecords(List<? extends ParsedRecord<?>> records, ImportContext importContext);
}
