package at.shiftcontrol.shiftservice.service.impl;

import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureTestDatabase
@WithMockUser(authorities = "USER")
public class PositionSlotServiceIT {

    @Autowired
    private PositionSlotServiceImpl positionSlotService;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    TestEntityFactory testEntityFactory;

    @MockitoBean
    UserProfileService userProfileService;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    SecurityHelper securityHelper;

    @Test
    void testCreateAuction() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long positionSlotId = 11L;

        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(positionSlotId, userId).get();
        AssignmentDto dto = positionSlotService.createAuction(assignment.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.AUCTION), dto.getStatus());
        Assertions.assertEquals(userId, assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(positionSlotId), dto.getPositionSlotId());
    }

    @Test
    @Transactional
    void testClaimAuction() {
        String auctionUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e7";
        long positionSlotId = 5L;
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(currentUserId));

        Assignment auction = assignmentRepository.findAssignmentForPositionSlotAndUser(positionSlotId, auctionUserId).get();
        PositionSlotRequestDto dto = testEntityFactory.getPositionSlotRequestDto(positionSlotId);

        AssignmentDto assignment = positionSlotService.claimAuction(auction.getPositionSlot().getId(), auctionUserId, currentUserId, dto);

        Assertions.assertNotNull(assignment);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), assignment.getStatus());
        Assertions.assertEquals(currentUserId, assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(positionSlotId), assignment.getPositionSlotId());
    }

    @Test
    void testCancelAuction() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long positionSlotId = 11L;

        Assignment auction = assignmentRepository.findAssignmentForPositionSlotAndUser(positionSlotId, userId).get();
        AssignmentDto assignmentDto = positionSlotService.cancelAuction(auction.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(assignmentDto);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), assignmentDto.getStatus());
        Assertions.assertEquals(userId, assignmentDto.getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(positionSlotId), assignmentDto.getPositionSlotId());
    }

    @Test
    void testLeave() throws ForbiddenException, NotFoundException {
        long positionSlotId = 1L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(userId));

        positionSlotService.leave(positionSlotId, userId);

        Assertions.assertFalse(assignmentRepository.findById(AssignmentId.of(positionSlotId, userId)).isPresent());
    }

    @Test
    @Transactional
    void testJoin() throws ForbiddenException, NotFoundException {
        long positionSlotId = 1L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(userId));

        PositionSlotRequestDto dto = testEntityFactory.getPositionSlotRequestDto(positionSlotId);
        AssignmentDto assignment = positionSlotService.join(positionSlotId, userId, dto);

        Assertions.assertNotNull(assignment);
        Assertions.assertEquals(userId, assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(positionSlotId), assignment.getPositionSlotId());
    }

    @Test
    @Transactional
    void testJoinRequest() throws ForbiddenException, NotFoundException {
        long positionSlotId = 9L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(userId));

        AssignmentDto assignment = positionSlotService.joinRequest(positionSlotId, userId);

        Assertions.assertNotNull(assignment);
        Assertions.assertEquals(AssignmentStatus.REQUEST_FOR_ASSIGNMENT, assignment.getStatus());
        Assertions.assertEquals(userId, assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(positionSlotId), assignment.getPositionSlotId());
    }

    @Test
    @Transactional
    void testLeaveRequest() throws ForbiddenException, NotFoundException {
        long positionSlotId = 9L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(userId));

        positionSlotService.leaveRequest(positionSlotId, userId);

        Optional<Assignment> optionalAssignment = assignmentRepository.findById(AssignmentId.of(positionSlotId, userId));
        Assertions.assertTrue(optionalAssignment.isPresent());
        Assignment assignment = optionalAssignment.get();
        Assertions.assertEquals(AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN, assignment.getStatus());
        Assertions.assertEquals(userId, assignment.getAssignedVolunteer().getId());
        Assertions.assertEquals(positionSlotId, assignment.getPositionSlot().getId());
    }
}
