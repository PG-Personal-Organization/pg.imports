package pg.imports.plugin.infrastructure.persistence.records.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<RecordEntity, UUID> {
}
