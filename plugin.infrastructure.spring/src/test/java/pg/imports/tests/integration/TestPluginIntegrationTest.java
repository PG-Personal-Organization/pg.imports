package pg.imports.tests.integration;

import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import pg.imports.config.ImportsIntegrationTest;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.imports.ImportStatus;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ImportsIntegrationTest
@EmbeddedKafka(brokerProperties = {"transaction.max.timeout.ms=3600000"})
@Testcontainers
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class TestPluginIntegrationTest {
    private final ImportRepository importRepository;
    private final RecordsRepository recordsRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    @Test
    @SneakyThrows
    void shouldParseRecordsCorrectly() {
        // given
        String file = "test/data/import_1.csv";
        try (InputStream fileInputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            var fileId = RestAssured
                    .given()
                    .multiPart("file", "import_1.csv", fileInputStream)

                    .when()
                    .post("/api/v1/files/rest/upload")

                    .then()
                    .statusCode(200)
                    .extract().as(UUID.class);

            // when
            var importId = RestAssured
                    .given()
                    .param("fileId", fileId)

                    .when()
                    .post("/api/v1/imports/start/{fileId}/{pluginCode}", fileId, "TEST")

                    .then()
                    .statusCode(200)
                    .extract().as(ImportId.class);

            // then
            await()
                    .atMost(30, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .until(() -> {
                        var importEntity = importRepository.findByIdAndStatus(importId.id(), ImportStatus.PARSING_FINISHED);
                        return importEntity.isPresent();
                    });

            var records = recordsRepository.findAllByParentImportId(importId);
            Assertions.assertNotEquals(0, records.size());
        }
    }
}
