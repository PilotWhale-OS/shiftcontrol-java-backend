package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentSwitchRequestRepositoryTest {
    @Autowired
    private AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Test
    void testGetAllAssignmentSwitchRequests() {
        List<AssignmentSwitchRequest> assignmentSwitchRequests = assignmentSwitchRequestRepository.findAll();
        Assertions.assertFalse(assignmentSwitchRequests.isEmpty());
    }

    @Test
    void testCancelTradesForAssignment() {
        AssignmentSwitchRequest trade = assignmentSwitchRequestRepository.findById(new AssignmentSwitchRequestId(
            new AssignmentId(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5"),
            new AssignmentId(2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6")
        )).orElseThrow(() -> new RuntimeException("trade not found"));

        Assertions.assertNotEquals(TradeStatus.CANCELED, trade.getStatus());
        assignmentSwitchRequestRepository.cancelTradesForAssignment(
            1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5", TradeStatus.CANCELED);

        trade = assignmentSwitchRequestRepository.findById(new AssignmentSwitchRequestId(
            new AssignmentId(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5"),
            new AssignmentId(2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6")
        )).orElseThrow(() -> new RuntimeException("trade not found"));
        Assertions.assertEquals(TradeStatus.CANCELED, trade.getStatus());
    }

    @Test
    void testFindOpenTradesForRequestedPositionAndOfferingUser() {
        long positionSlotId = 2L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Collection<AssignmentSwitchRequest> trades =
            assignmentSwitchRequestRepository.findOpenTradesForRequestedPositionAndOfferingUser(positionSlotId, userId, TradeStatus.OPEN);

        Assertions.assertFalse(trades.isEmpty());
        AssignmentSwitchRequest trade = trades.stream().findFirst().get();
        Assertions.assertEquals(positionSlotId, trade.getRequestedAssignment().getPositionSlot().getId());
        Assertions.assertEquals(userId, trade.getOfferingAssignment().getAssignedVolunteer().getId());
    }

    @Test
    void testFindTradesForShiftPlanAndUser() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long shiftPlanId  = 1L;

        Collection<AssignmentSwitchRequest> trades =
            assignmentSwitchRequestRepository.findTradesForShiftPlanAndUser(shiftPlanId, userId);

        Assertions.assertFalse(trades.isEmpty());
        trades.forEach( t -> {
            Assertions.assertTrue(
                t.getRequestedAssignment().getAssignedVolunteer().getId().equals(userId)
                    || t.getOfferingAssignment().getAssignedVolunteer().getId().equals(userId));
            Assertions.assertTrue(
                t.getRequestedAssignment().getPositionSlot().getShift().getShiftPlan().getId() == shiftPlanId
                    || t.getOfferingAssignment().getPositionSlot().getShift().getShiftPlan().getId() == shiftPlanId);
        });
    }

    @Test
    void deleteTradesForOfferedPositionAndRequestedUser() {
        long positionSlotId = 1L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";

        assignmentSwitchRequestRepository.deleteTradesForOfferedPositionAndRequestedUser(positionSlotId, userId);


    }

    @Test
    void testDeleteTradesForAssignment() {
        long positionSlotId = 3L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e7";

        assignmentSwitchRequestRepository.deleteTradesForAssignment(positionSlotId, userId);


    }

}
