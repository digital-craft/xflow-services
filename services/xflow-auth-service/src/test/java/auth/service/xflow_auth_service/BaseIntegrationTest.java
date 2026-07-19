package auth.service.xflow_auth_service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("xflow_auth_test")
                .withUsername("xflow_test")
                .withPassword("xflow_test");
        postgresContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String host = "host.docker.internal";
        Integer mappedPort = postgresContainer.getFirstMappedPort();
        String url = String.format("jdbc:postgresql://%s:%d/%s", 
            host, 
            mappedPort, 
            postgresContainer.getDatabaseName());
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}