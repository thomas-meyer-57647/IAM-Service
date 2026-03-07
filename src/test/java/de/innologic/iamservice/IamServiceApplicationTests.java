package de.innologic.iamservice;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class IamServiceApplicationTests {

    @Autowired
    Flyway flyway;

    @Test
    void contextLoads() {
        // ensures Flyway is wired and migrations ran on the H2 test database
        assert flyway.info().current() != null;
    }
}
