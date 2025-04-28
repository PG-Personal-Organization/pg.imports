package pg.plugin.api.parsing;

import pg.plugin.api.strategies.db.RecordData;

public interface RecordParser<IN, OUT extends RecordData> {
    OUT parse(ReaderOutputItem<IN> item);
}
