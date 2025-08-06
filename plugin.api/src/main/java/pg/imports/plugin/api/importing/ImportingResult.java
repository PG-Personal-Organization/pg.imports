package pg.imports.plugin.api.importing;

import pg.imports.plugin.api.strategies.db.RecordData;

import java.util.Optional;

public interface ImportingResult<RECORD extends RecordData> {

    Optional<String> importingErrorCode();
}
