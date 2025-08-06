package pg.imports.plugin.api.parsing;

import lombok.NonNull;
import pg.imports.plugin.api.data.ImportContext;
import pg.imports.plugin.api.strategies.db.RecordData;

public interface RecordParser<RECORD_DATA extends RecordData, OUT extends ParsedRecord<RECORD_DATA>> {
    @NonNull OUT parse(ReaderOutputItem<Object> item, ImportContext importContext);
}
