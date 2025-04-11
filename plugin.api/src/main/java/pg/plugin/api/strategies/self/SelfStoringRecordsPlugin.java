package pg.plugin.api.strategies.self;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.plugin.api.ImportPlugin;
import pg.plugin.api.strategies.db.RecordData;

/**
 * Used with {@link pg.plugin.api.strategies.RecordsStoring#PLUGIN_DATABASE}
 * */
public interface SelfStoringRecordsPlugin<T extends RecordData> extends ImportPlugin {
    JpaRepository<T, String> getRecordRepository();

    Class<T> getRecordClass();
}
