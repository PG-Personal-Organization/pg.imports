package pg.plugin.infrastructure.records.persistence.internal.records.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import pg.plugin.api.strategies.mongo.RecordDocument;

import java.util.UUID;

public interface MongoRecordRepository extends MongoRepository<RecordDocument, UUID> {
}
