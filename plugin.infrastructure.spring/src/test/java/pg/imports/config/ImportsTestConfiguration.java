package pg.imports.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import pg.context.auth.api.context.provider.ContextProvider;
import pg.context.auth.api.security.UserSecurityFilter;
import pg.context.auth.domain.context.UserContext;
import pg.context.auth.domain.context.UserContextNotFoundException;
import pg.imports.tests.data.DistributedTestPlugin;
import pg.imports.tests.data.ParallelMongoTestPlugin;
import pg.imports.tests.data.ParallelTestPlugin;
import pg.imports.tests.data.SimpleTestPlugin;
import pg.imports.tests.data.common.importing.InMemoryImportedPaymentsRepository;
import pg.imports.tests.data.common.importing.TestImportingComponentsProvider;
import pg.imports.tests.data.common.importing.TestRecordImporter;
import pg.imports.tests.data.common.parsing.TestParsingComponentsProvider;
import pg.imports.tests.data.common.parsing.TestRecordParser;
import pg.lib.awsfiles.infrastructure.mock.InMemoryMockConfiguration;
import pg.lib.common.spring.auth.HeaderAuthenticationFilter;
import pg.lib.common.spring.config.CommonModuleConfiguration;
import pg.lib.common.spring.storage.HeadersHolder;

import java.util.Optional;
import java.util.Set;

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
    public InMemoryImportedPaymentsRepository importedPaymentsRepository() {
        return new InMemoryImportedPaymentsRepository();
    }

    @Bean
    public TestRecordImporter testRecordImporter() {
        return new TestRecordImporter(importedPaymentsRepository());
    }

    @Bean
    public TestImportingComponentsProvider testImportingComponentsProvider() {
        return new TestImportingComponentsProvider(testRecordImporter());
    }

    @Bean
    public SimpleTestPlugin simpleTestPlugin() {
        return new SimpleTestPlugin(testParsingComponentsProvider(), testImportingComponentsProvider());
    }

    @Bean
    public ParallelTestPlugin parallelTestPlugin() {
        return new ParallelTestPlugin(testParsingComponentsProvider(), testImportingComponentsProvider());
    }

    @Bean
    public ParallelMongoTestPlugin parallelMongoTestPlugin() {
        return new ParallelMongoTestPlugin(testParsingComponentsProvider(), testImportingComponentsProvider());
    }

    @Bean
    public DistributedTestPlugin distributedTestPlugin() {
        return new DistributedTestPlugin(testParsingComponentsProvider(), testImportingComponentsProvider());
    }

    @Bean
    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> importsTestSecurityCustomizer() {
        return requests -> requests.anyRequest().permitAll();
    }

    @Bean
    public ContextProvider mockContextProvider() {
        return new ContextProvider() {
            static final UserContext MOCK_CONTEXT = UserContext.builder()
                    .userId("MOCK_USER_ID")
                    .contextToken("MOCK_CONTEXT_TOKEN")
                    .username("MOCK_USER")
                    .roles(Set.of("USER"))
                    .build();

            @Override
            public Optional<UserContext> tryToGetUserContext(final String contextToken) {
                return Optional.of(MOCK_CONTEXT);
            }

            @Override
            public Optional<UserContext> tryToGetUserContext() {
                return Optional.of(MOCK_CONTEXT);
            }

            @Override
            public UserContext getUserContext(final String contextToken) throws UserContextNotFoundException {
                return MOCK_CONTEXT;
            }
        };
    }

    @Bean
    public HeaderAuthenticationFilter userAuthenticationFilter(final HeadersHolder headersHolder) {
        return new UserSecurityFilter(mockContextProvider(), headersHolder);
    }
}
