package pg.imports.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import pg.imports.tests.data.*;
import pg.lib.awsfiles.infrastructure.config.InMemoryMockConfiguration;
import pg.lib.common.spring.config.CommonModuleConfiguration;

@Import({
        CommonModuleConfiguration.class,
        InMemoryMockConfiguration.class,
})
@TestConfiguration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportsTestConfiguration {

    @Bean
    public TestParsingComponentsProvider testParsingComponentsProvider() {
        return new TestParsingComponentsProvider(new TestRecordParser());
    }

    @Bean
    public SimpleTestPlugin simpleTestPlugin() {
        return new SimpleTestPlugin(testParsingComponentsProvider(), null);
    }

    @Bean
    public ParallelTestPlugin parallelTestPlugin() {
        return new ParallelTestPlugin(testParsingComponentsProvider(), null);
    }

    @Bean
    public DistributedTestPlugin distributedTestPlugin() {
        return new DistributedTestPlugin(testParsingComponentsProvider(), null);
    }

    @Bean
    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> importsTestSecurityCustomizer() {
        return requests -> requests.anyRequest().permitAll();
    }
}
