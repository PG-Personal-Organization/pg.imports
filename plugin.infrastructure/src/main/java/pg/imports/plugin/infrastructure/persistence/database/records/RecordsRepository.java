package pg.imports.plugin.infrastructure.persistence.database.records;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordsRepository extends JpaRepository<ImportRecordsEntity, String> {

    @SuppressWarnings("checkstyle:MethodName")
    List<ImportRecordsEntity> findAllByParent_Id(String importId);
}
