package config;

import at.shiftcontrol.shiftservice.util.TestEntityFactory;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
public class TestConfig {

    @Bean
    public TestEntityFactory testEntityFactory() {
        return new TestEntityFactory();
    }

}
