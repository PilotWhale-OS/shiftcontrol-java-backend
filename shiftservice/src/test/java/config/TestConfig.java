package config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import at.shiftcontrol.shiftservice.service.impl.rewardpoints.RewardPointsCalculatorImpl;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;

@TestConfiguration
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
public class TestConfig {
    @Bean
    public TestEntityFactory testEntityFactory() {
        return new TestEntityFactory();
    }
    @Bean
    public RewardPointsCalculator rewardPointsCalculator() {
        return new RewardPointsCalculatorImpl();
    }
}
