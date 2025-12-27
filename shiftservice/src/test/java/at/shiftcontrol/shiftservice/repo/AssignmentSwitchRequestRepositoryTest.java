package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
}
