package at.shiftcontrol.shiftservice.repo;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteShiftPlanAccessRepository;

@DataJpaTest
@Import({TestConfig.class})
public class UserInviteRepositoryTest {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShiftPlanRepository shiftPlanRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserInviteRepository userInviteRepository;

    @Autowired
    private UserInviteShiftPlanAccessRepository userInviteShiftPlanAccessRepository;

    @Test
    void saveInviteWithPendingPlanAccessAndRoles() {
        Event event = eventRepository.save(Event.builder()
            .name("Directory migration event")
            .startTime(Instant.parse("2026-07-01T08:00:00Z"))
            .endTime(Instant.parse("2026-07-01T18:00:00Z"))
            .build());

        ShiftPlan shiftPlan = shiftPlanRepository.save(ShiftPlan.builder()
            .event(event)
            .name("Planner onboarding plan")
            .lockStatus(LockStatus.SELF_SIGNUP)
            .defaultNoRolePointsPerMinute(0)
            .build());

        Role role = roleRepository.save(Role.builder()
            .shiftPlan(shiftPlan)
            .name("Arrival Desk")
            .description("Can be granted before first login.")
            .selfAssignable(false)
            .rewardPointsPerMinute(2)
            .build());

        UserInvite userInvite = UserInvite.builder()
            .code("invite-code-001")
            .email("future.planner@example.com")
            .firstName("Future")
            .lastName("Planner")
            .displayName("Future Planner")
            .status(UserInviteStatus.PENDING)
            .expiresAt(Instant.parse("2026-07-15T00:00:00Z"))
            .pendingRoles(List.of(role))
            .build();

        userInvite.addPendingShiftPlanAccess(UserInviteShiftPlanAccess.builder()
            .shiftPlan(shiftPlan)
            .accessType(UserInviteShiftPlanAccessType.PLANNER)
            .build());

        UserInvite savedUserInvite = userInviteRepository.save(userInvite);
        UserInvite resolvedInvite = userInviteRepository.findByCode("invite-code-001").orElseThrow();

        Assertions.assertAll(
            () -> Assertions.assertEquals(savedUserInvite.getId(), resolvedInvite.getId()),
            () -> Assertions.assertEquals(UserInviteStatus.PENDING, resolvedInvite.getStatus()),
            () -> Assertions.assertEquals(1, resolvedInvite.getPendingRoles().size()),
            () -> Assertions.assertEquals(1, userInviteShiftPlanAccessRepository.findAllByUserInviteId(savedUserInvite.getId()).size()),
            () -> Assertions.assertEquals(
                UserInviteShiftPlanAccessType.PLANNER,
                userInviteShiftPlanAccessRepository.findAllByUserInviteId(savedUserInvite.getId()).iterator().next().getAccessType()
            ),
            () -> Assertions.assertTrue(
                userInviteRepository.findFirstByEmailIgnoreCaseAndStatus("FUTURE.PLANNER@example.com", UserInviteStatus.PENDING).isPresent()
            )
        );
    }
}
