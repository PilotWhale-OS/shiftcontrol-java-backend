package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;

@DataJpaTest
@Import({TestConfig.class})
public class ActivityRepositoryTest {
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private TestEntityFactory testEntityFactory;

    @Test
    void testGetAllActivities() {
        List<Activity> activities = activityRepository.findAll();
        Assertions.assertFalse(activities.isEmpty());
    }

    @Test
    void testSaveActivity() {
        Activity factoryPersistedActivity = testEntityFactory.createPersistedActivity();
        Assertions.assertNotNull(factoryPersistedActivity);
    }
}
