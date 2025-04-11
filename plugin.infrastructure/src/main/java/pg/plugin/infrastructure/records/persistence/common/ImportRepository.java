package pg.plugin.infrastructure.records.persistence.common;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRepository extends JpaRepository<ImportEntity, String> {
}
