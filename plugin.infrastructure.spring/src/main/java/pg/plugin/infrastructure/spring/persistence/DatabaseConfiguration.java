package pg.plugin.infrastructure.spring.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("pg.plugin.infrastructure.persistence")
@EnableJpaRepositories("pg.plugin.infrastructure.persistence")
@Configuration
public class DatabaseConfiguration {
}
