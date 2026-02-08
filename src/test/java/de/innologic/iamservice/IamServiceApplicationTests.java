package de.innologic.iamservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class IamServiceApplicationTests {

    @Container
    static MariaDBContainer<?> db = new MariaDBContainer<>("mariadb:10.4")
            .withDatabaseName("iam")
            .withUsername("iam")
            .withPassword("iam");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", db::getJdbcUrl);
        r.add("spring.datasource.username", db::getUsername);
        r.add("spring.datasource.password", db::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }

    @Test
    void contextLoads() { }
}
