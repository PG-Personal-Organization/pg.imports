package pg.plugin.infrastructure.persistence.records.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoRecordRepository extends MongoRepository<RecordDocument, UUID> {
}
