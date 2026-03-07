package de.innologic.iamservice.persistence;

import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.module.repo.IamModuleRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayH2SmokeTest {

    @Autowired
    Flyway flyway;

    @Autowired
    IamModuleRepository moduleRepository;

    @Test
    void flywayMigratesAndJpaRepositoryWorks() {
        assertThat(flyway.info().current()).isNotNull();

        IamModuleEntity module = new IamModuleEntity();
        module.setModuleKey("smoke-test");
        module.setName("Smoke Test Module");
        module.setDescription("Ensures Flyway + H2 wiring");
        moduleRepository.save(module);

        assertThat(moduleRepository.findByModuleKey("smoke-test")).isPresent();
    }
}
