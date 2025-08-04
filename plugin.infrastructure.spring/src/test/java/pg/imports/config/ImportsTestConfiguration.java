package pg.imports.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import pg.lib.common.spring.config.CommonModuleConfiguration;

@Import({
        CommonModuleConfiguration.class
})
@TestConfiguration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportsTestConfiguration {

}
