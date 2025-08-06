package pg.imports.plugin.api.importing;

import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

public interface RecordImporter<RECORD extends RecordData, IN extends ParsedRecord<RecordData>, IN_PROVIDER extends ImportingRecordsProvider<IN>> {
    ImportingResult<RECORD> importRecords(IN_PROVIDER provider);
}
