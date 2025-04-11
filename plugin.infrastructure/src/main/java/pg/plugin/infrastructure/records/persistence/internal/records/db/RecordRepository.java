package pg.plugin.infrastructure.records.persistence.internal.records.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<RecordEntity, UUID> {
}
