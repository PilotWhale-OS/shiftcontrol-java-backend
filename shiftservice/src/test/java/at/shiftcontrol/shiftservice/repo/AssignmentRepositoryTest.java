package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentRepositoryTest {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private PositionSlotRepository positionSlotRepository;

    @Test
    void testGetAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        Assertions.assertFalse(assignments.isEmpty());
    }

    @Test
    void testFindAuctionsByShiftPlanId() {
        Collection<Assignment> assignments = assignmentRepository.findAuctionsByShiftPlanId(2L);
        Assertions.assertFalse(assignments.isEmpty());
        assignments.forEach(
            a -> Assertions.assertAll(
                () -> Assertions.assertEquals(2L, a.getPositionSlot().getShift().getShiftPlan().getId()),
                () -> Assertions.assertTrue(
                    EnumSet.of(AssignmentStatus.AUCTION, AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN).contains(a.getStatus())
                )
            )
        );
    }

    @Test
    void testGetConflictingAssignments() {
        // volunteer 5 is assigned to a position 1  from 2025-09-12T13:00:00Z to 2025-09-12T12:00:00Z
        // the shift of position slot 21 is         from 2025-09-12T12:00:00Z to 2025-09-12T13:00:01Z (1s overlap)
        PositionSlot positionSlot = positionSlotRepository.getReferenceById(21L);

        Collection<Assignment> assignments = assignmentRepository.getConflictingAssignments(
            5L, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime()
        );
        Assertions.assertFalse(assignments.isEmpty());

    }
}
