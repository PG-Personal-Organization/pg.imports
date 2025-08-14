package pg.imports.plugin.infrastructure.spring.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("pg.imports.plugin.infrastructure.persistence")
@EnableJpaRepositories("pg.imports.plugin.infrastructure.persistence")
@ComponentScan("pg.imports.plugin.infrastructure.persistence")
@Configuration
public class DatabaseConfiguration {
}
