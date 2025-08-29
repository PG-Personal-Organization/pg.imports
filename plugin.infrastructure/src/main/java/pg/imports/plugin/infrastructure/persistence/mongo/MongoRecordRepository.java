package pg.imports.plugin.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoRecordRepository extends MongoRepository<RecordDocument, String> {
}
