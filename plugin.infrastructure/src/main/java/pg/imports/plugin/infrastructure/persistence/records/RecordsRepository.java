package pg.imports.plugin.infrastructure.persistence.records;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.imports.plugin.api.data.ImportId;

import java.util.List;

public interface RecordsRepository extends JpaRepository<ImportRecordsEntity, String> {

    List<ImportRecordsEntity> findAllByParentImportId(ImportId importId);
}
