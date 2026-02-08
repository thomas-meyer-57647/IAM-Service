package de.innologic.iamservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class IamServiceApplicationTests extends MySqlTestContainerConfig {

    @Test
    void contextLoads() {}
}
