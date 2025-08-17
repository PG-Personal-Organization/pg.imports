package pg.imports.plugin.api.importing;

import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.List;

public interface ImportingRecordsProvider<RECORD extends ParsedRecord<? extends RecordData>> {

    List<RECORD> getRecords();
}
