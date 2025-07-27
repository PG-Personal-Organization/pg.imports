package pg.kafka.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pg.lib.common.spring.config.CommonModuleConfiguration;

@Import({
        CommonModuleConfiguration.class
})
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportsTestConfiguration {
}
