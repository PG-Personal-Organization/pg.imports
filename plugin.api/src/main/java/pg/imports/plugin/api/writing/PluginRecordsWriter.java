package pg.imports.plugin.api.writing;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.List;

public interface PluginRecordsWriter<RECORD extends RecordData> {
    @NonNull
    WrittenRecords writeRecords(List<? extends ParsedRecord<?>> records, ImportContext importContext);
}
