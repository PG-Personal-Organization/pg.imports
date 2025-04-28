package pg.plugin.infrastructure.persistence.records;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordsRepository extends JpaRepository<ImportRecordsEntity, String> {
}
