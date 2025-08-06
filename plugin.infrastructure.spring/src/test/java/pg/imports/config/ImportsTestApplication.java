package pg.imports.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import pg.imports.plugin.infrastructure.spring.common.ImportPluginConfiguration;

@Import({
        ImportsTestConfiguration.class,
        ImportPluginConfiguration.class,
})
@SpringBootApplication
public class ImportsTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImportsTestApplication.class, args);
    }
}
