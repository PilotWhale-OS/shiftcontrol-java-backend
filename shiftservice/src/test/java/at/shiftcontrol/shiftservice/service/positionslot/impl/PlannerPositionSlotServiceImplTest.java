package at.shiftcontrol.shiftservice.service.positionslot.impl;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.mapper.AssignmentAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.AssignmentPlannerInfoAssemblingMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerPositionSlotServiceImplTest {
    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private ShiftPlanDao shiftPlanDao;

    @Mock
    private AssignmentDao assignmentDao;

    @Mock
    private PositionSlotDao positionSlotDao;

    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private AssignmentAssemblingMapper assignmentAssemblingMapper;

    @Mock
    private AssignmentPlannerInfoAssemblingMapper assignmentRequestAssemblingMapper;

    @InjectMocks
    private PlannerPositionSlotServiceImpl plannerPositionSlotService;

    @Test
    void getAssignableUsers_filtersVolunteersBeforeResolvingDirectoryProfiles() {
        ShiftPlan shiftPlan = new ShiftPlan();
        shiftPlan.setId(9L);
        Shift shift = new Shift();
        shift.setId(4L);
        shift.setShiftPlan(shiftPlan);
        PositionSlot positionSlot = new PositionSlot();
        positionSlot.setId(5L);
        positionSlot.setShift(shift);

        Volunteer eligibleVolunteer = new Volunteer();
        eligibleVolunteer.setId("user-1");
        Volunteer ineligibleVolunteer = new Volunteer();
        ineligibleVolunteer.setId("user-2");

        when(positionSlotDao.getById(5L)).thenReturn(positionSlot);
        when(volunteerDao.findAllByShiftPlan(9L)).thenReturn(List.of(eligibleVolunteer, ineligibleVolunteer));
        when(eligibilityService.isEligibleAndNotSignedUp(positionSlot, eligibleVolunteer)).thenReturn(true);
        when(eligibilityService.isEligibleAndNotSignedUp(positionSlot, ineligibleVolunteer)).thenReturn(false);
        when(eligibilityService.getConflictingAssignmentsExcludingShift("user-1", positionSlot, 4L)).thenReturn(List.of());
        when(userDirectoryService.getUserByIds(List.of("user-1"))).thenReturn(List.of(
            new DirectoryUser("user-1", "alice", "Alice", "Admin", "alice@example.com", "https://cdn.example.test/profiles/user-1.png", false)
        ));

        var result = plannerPositionSlotService.getAssignableUsers("5");

        assertThat(result)
            .extracting(VolunteerDto::getId, VolunteerDto::getFirstName)
            .containsExactly(org.assertj.core.groups.Tuple.tuple("user-1", "Alice"));
        verify(securityHelper).assertUserIsPlanner(positionSlot);
    }
}
