package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@SpringBootTest
@AutoConfigureTestDatabase
public class PositionSlotServiceIT {

    @Autowired
    private PositionSlotServiceImpl positionSlotService;
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Test
    void testCreateAuction() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";

        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(4L, userId);
        AssignmentDto dto = positionSlotService.createAuction(assignment.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.AUCTION), dto.getStatus());
    }

    @Test
    void testClaimAuction() throws ConflictException, NotFoundException, ForbiddenException {
        String auctionUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e7";

        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(5L, auctionUserId);
        AssignmentDto dto = positionSlotService.claimAuction(assignment.getPositionSlot().getId(), auctionUserId, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), dto.getStatus());

    }

    @Test
    void testCancelAuction() throws ForbiddenException {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        // AssignedUser user = new AssignedUser(null, null, userId, null);
        // Mockito.when(userProviderMock.getCurrentUser()).thenReturn(user);

        Assignment auction = assignmentRepository.findAssignmentForPositionSlotAndUser(11L, userId);
        AssignmentDto dto = positionSlotService.cancelAuction(auction.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), dto.getStatus());
    }
}
