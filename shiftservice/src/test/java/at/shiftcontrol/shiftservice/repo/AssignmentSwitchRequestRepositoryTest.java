package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.type.TradeStatus;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentSwitchRequestRepositoryTest {
    @Autowired
    private AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Autowired
    EntityManager em;

    @Test
    void testGetAllAssignmentSwitchRequests() {
        List<AssignmentSwitchRequest> assignmentSwitchRequests = assignmentSwitchRequestRepository.findAll();
        Assertions.assertFalse(assignmentSwitchRequests.isEmpty());
    }

    @Test
    void testCancelTradesForAssignment() {
        AssignmentSwitchRequest trade = assignmentSwitchRequestRepository.findBySlotsAndUsers(
            1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5",
            2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6"
        ).orElseThrow(() -> new RuntimeException("trade not found"));

        Assertions.assertNotEquals(TradeStatus.CANCELED, trade.getStatus());
        assignmentSwitchRequestRepository.cancelTradesForAssignment(
            1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5", TradeStatus.CANCELED);

        em.flush();
        em.clear();

        trade = assignmentSwitchRequestRepository.findBySlotsAndUsers(
            1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5",
            2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6"
        ).orElseThrow(() -> new RuntimeException("trade not found"));

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
        long shiftPlanId = 1L;

        Collection<AssignmentSwitchRequest> trades =
            assignmentSwitchRequestRepository.findTradesForShiftPlanAndUser(shiftPlanId, userId);

        Assertions.assertFalse(trades.isEmpty());
        trades.forEach(t -> {
            Assertions.assertTrue(
                t.getRequestedAssignment().getAssignedVolunteer().getId().equals(userId)
                    || t.getOfferingAssignment().getAssignedVolunteer().getId().equals(userId));
            Assertions.assertTrue(
                t.getRequestedAssignment().getPositionSlot().getShift().getShiftPlan().getId() == shiftPlanId
                    || t.getOfferingAssignment().getPositionSlot().getShift().getShiftPlan().getId() == shiftPlanId);
        });
    }

    @Test
    void testCancelTradesForOfferedPositionAndRequestedUser() {
        long offeredSlotId = 3L;
        String offeringUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e7";
        long requestedSlotId = 4L;
        String requestingUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";

        assignmentSwitchRequestRepository.cancelTradesForOfferedPositionAndRequestedUser(offeredSlotId, requestingUserId, TradeStatus.CANCELED);

        Optional<AssignmentSwitchRequest> trade = assignmentSwitchRequestRepository.findBySlotsAndUsers(offeredSlotId, offeringUserId, requestedSlotId, requestingUserId);
        Assertions.assertTrue(trade.isPresent());
        Assertions.assertEquals(TradeStatus.CANCELED, trade.get().getStatus());
    }

}
