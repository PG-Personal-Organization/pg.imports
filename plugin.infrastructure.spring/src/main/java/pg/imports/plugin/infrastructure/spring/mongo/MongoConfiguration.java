package pg.imports.plugin.infrastructure.spring.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("pg.imports.plugin.infrastructure.persistence.mongo")
public class MongoConfiguration {
}
