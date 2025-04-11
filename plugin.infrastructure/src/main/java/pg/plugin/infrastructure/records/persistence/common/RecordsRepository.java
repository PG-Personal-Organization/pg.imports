package pg.plugin.infrastructure.records.persistence.common;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordsRepository extends JpaRepository<ImportRecordsEntity, String> {
}
