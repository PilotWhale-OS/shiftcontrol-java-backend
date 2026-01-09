package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Volunteer;

@DataJpaTest
@Import({TestConfig.class})
public class VolunteerRepositoryTest {
    @Autowired
    private VolunteerRepository volunteerRepository;

    @Test
    void testGetAllVolunteers() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        Assertions.assertFalse(volunteers.isEmpty());
    }

    @Test
    void testFindAllByShiftPlan() {
        long shiftPlanId = 1L;

        Collection<Volunteer> volunteers = volunteerRepository.findAllByShiftPlan(shiftPlanId);

        Assertions.assertFalse(volunteers.isEmpty());
        volunteers.forEach(v -> Assertions.assertTrue(
            v.getVolunteeringPlans().stream().anyMatch(plan -> plan.getId() == shiftPlanId)
        ));
    }

    @Test
    void testFindAllByShiftPlanAndVolunteerIds() {
        long shiftPlanId = 1L;
        Collection<String> volunteerIds = List.of(
            "28c02050-4f90-4f3a-b1df-3c7d27a166e5",
            "28c02050-4f90-4f3a-b1df-3c7d27a166e6"
        );

        Collection<Volunteer> volunteers = volunteerRepository.findAllByShiftPlanAndVolunteerIds(shiftPlanId, volunteerIds);

        Assertions.assertEquals(volunteerIds.size(), volunteers.size());
        volunteers.forEach(v -> Assertions.assertAll(
            () -> Assertions.assertTrue(
                v.getVolunteeringPlans().stream().anyMatch(plan -> plan.getId() == shiftPlanId)),
            () -> Assertions.assertTrue(volunteerIds.contains(v.getId()))
        ));
    }
}
