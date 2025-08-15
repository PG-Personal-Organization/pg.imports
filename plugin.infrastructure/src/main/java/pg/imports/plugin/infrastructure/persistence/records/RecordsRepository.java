package pg.imports.plugin.infrastructure.persistence.records;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecordsRepository extends JpaRepository<ImportRecordsEntity, String> {

    @Query("SELECT r FROM imported_records r WHERE r.parent.id = :importId")
    List<ImportRecordsEntity> findAllByParentImportId(String importId);
}
