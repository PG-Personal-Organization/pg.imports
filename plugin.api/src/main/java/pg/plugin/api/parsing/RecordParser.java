package pg.plugin.api.parsing;

import lombok.NonNull;
import pg.plugin.api.strategies.db.RecordData;

public interface RecordParser<IN, RECORD_DATA extends RecordData, OUT extends ParsedRecord<RECORD_DATA>> {
    @NonNull OUT parse(ReaderOutputItem<IN> item);
}
