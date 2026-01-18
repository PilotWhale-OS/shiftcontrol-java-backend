package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.type.AssignmentStatus;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentRepositoryTest {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;
    @Autowired
    private PositionSlotRepository positionSlotRepository;

    @Test
    void testGetAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        Assertions.assertFalse(assignments.isEmpty());
    }

    @Test
    void testFindSignupRequestsByShiftPlanId() {
        long shiftPlanId = 1L;

        Collection<Assignment> assignments = assignmentRepository.findSignupRequestsByShiftPlanId(shiftPlanId, AssignmentStatus.REQUEST_FOR_ASSIGNMENT);

        Assertions.assertFalse(assignments.isEmpty());
        assignments.forEach(
            a -> Assertions.assertAll(
                () -> Assertions.assertEquals(shiftPlanId, a.getPositionSlot().getShift().getShiftPlan().getId()),
                () -> Assertions.assertEquals(AssignmentStatus.REQUEST_FOR_ASSIGNMENT, a.getStatus())
            )
        );
    }

    @Test
    void testFindAuctionsByShiftPlanId() {
        long shiftPlanId = 3L;

        Collection<Assignment> assignments = assignmentRepository.findAuctionsByShiftPlanId(shiftPlanId, AssignmentStatus.ACTIVE_AUCTION_STATES);

        Assertions.assertFalse(assignments.isEmpty());
        assignments.forEach(
            a -> Assertions.assertAll(
                () -> Assertions.assertEquals(shiftPlanId, a.getPositionSlot().getShift().getShiftPlan().getId()),
                () -> Assertions.assertTrue(AssignmentStatus.ACTIVE_AUCTION_STATES.contains(a.getStatus())
                )
            )
        );
    }

    @Test
    void findAuctionsByShiftPlanIdExcludingUser() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long shiftPlanId = 3L;

        Collection<Assignment> assignments = assignmentRepository.findAuctionsByShiftPlanIdExcludingUser(shiftPlanId, userId, AssignmentStatus.ACTIVE_AUCTION_STATES);

        Assertions.assertFalse(assignments.isEmpty());
        assignments.forEach(
            a -> Assertions.assertAll(
                () -> Assertions.assertEquals(shiftPlanId, a.getPositionSlot().getShift().getShiftPlan().getId()),
                () -> Assertions.assertTrue(AssignmentStatus.ACTIVE_AUCTION_STATES.contains(a.getStatus())),
                () -> Assertions.assertNotEquals(userId, a.getAssignedVolunteer().getId())
            )
        );
    }

    @Test
    void testGetConflictingAssignments() {
        // volunteer is assigned to a position  from 2025-09-12T08:00:00Z to 2025-09-12T12:00:00Z
        // the shift of position slot 21 is     from 2025-09-12T07:00:00Z to 2025-09-12T08:00:01Z (1s overlap)
        PositionSlot positionSlot = positionSlotRepository.getReferenceById(21L);

        Collection<Assignment> assignments = assignmentRepository.getConflictingAssignments(
            "28c02050-4f90-4f3a-b1df-3c7d27a166e5", positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime()
        );
        Assertions.assertFalse(assignments.isEmpty());
    }

    @Test
    void testFindAssignmentForPositionSlotAndUser() {
        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5").get();
        Assertions.assertEquals("28c02050-4f90-4f3a-b1df-3c7d27a166e5", assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(1L, assignment.getPositionSlot().getId());
    }

    @Test
    void testFindAssignmentForPositionSlotAndUsers() {
        Collection<Assignment> assignments =
            assignmentRepository.findAssignmentForPositionSlotAndUsers(1L, List.of("28c02050-4f90-4f3a-b1df-3c7d27a166e5"));

        Assertions.assertFalse(assignments.isEmpty());
        Assignment assignment = assignments.stream().findFirst().get();
        Assertions.assertEquals("28c02050-4f90-4f3a-b1df-3c7d27a166e5", assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(1L, assignment.getPositionSlot().getId());
    }

    @Test
    void testDeleteCascadeToTrades() {
        long assignmentId = 1L;

        assignmentRepository.deleteById(assignmentId);
        assignmentRepository.flush();

        Assertions.assertFalse(assignmentRepository.findById(assignmentId).isPresent());

        Assertions.assertFalse(assignmentSwitchRequestRepository.findById(1L).isPresent());
    }
}
