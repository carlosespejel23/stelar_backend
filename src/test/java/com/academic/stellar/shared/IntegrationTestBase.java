package com.academic.stellar.shared;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.education.stelar.StelarApplication;
import com.education.stelar.kernel.multitenancy.TenantContext;

/**
 * Base para todos los tests de integración.
 * Levanta un PostgreSQL real via Testcontainers y lo comparte entre tests (reuse).
 */
@SpringBootTest(classes = StelarApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("stellar_test")
            .withUsername("stellar")
            .withPassword("stellar")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }
}
