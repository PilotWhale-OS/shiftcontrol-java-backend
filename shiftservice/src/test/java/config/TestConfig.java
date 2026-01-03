package config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import at.shiftcontrol.shiftservice.util.TestEntityFactory;

@TestConfiguration
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
public class TestConfig {
    @Bean
    public TestEntityFactory testEntityFactory() {
        return new TestEntityFactory();
    }
}
