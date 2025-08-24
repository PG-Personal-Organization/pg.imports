package pg.imports.plugin.infrastructure.persistence.records.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<RecordEntity, String> {
}
