package pg.plugin.api.parsing;

import pg.plugin.api.data.ImportedRecord;
import pg.plugin.api.errors.NotImplementedException;
import pg.plugin.api.strategies.db.RecordData;

public interface ParsingComponentsProvider<RD extends RecordData, IN, OUT extends ImportedRecord<RD>> {
    default ReaderDefinition getReaderDefinition() {
        throw new NotImplementedException("Reader definition not implemented for plugin");
    }

    default RecordParser<IN, RD> getRecordParser() {
        throw new NotImplementedException("Not implemented for plugin");
    }

}
