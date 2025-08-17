package pg.imports.plugin.api.importing;

import pg.imports.plugin.api.parsing.ParsedRecord;
import pg.imports.plugin.api.strategies.db.RecordData;

public interface RecordImporter<RECORD extends RecordData, IN extends ParsedRecord<RECORD>> {
    ImportingResult importRecords(ImportingRecordsProvider<IN> provider);
}
