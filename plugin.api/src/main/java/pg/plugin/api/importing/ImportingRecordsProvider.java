package pg.plugin.api.importing;

import pg.plugin.api.parsing.ParsedRecord;
import pg.plugin.api.strategies.db.RecordData;

import java.util.List;

public interface ImportingRecordsProvider<RECORD extends ParsedRecord<RecordData>> {

    List<RECORD> getRecords();
}
