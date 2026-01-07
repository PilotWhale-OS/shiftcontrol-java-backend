package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
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

    @MockitoBean
    UserProfileService userProfileService;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    SecurityHelper securityHelper;

    @Test
    void testCreateAuction() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";

        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(11L, userId).get();
        AssignmentDto dto = positionSlotService.createAuction(assignment.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.AUCTION), dto.getStatus());
    }

    @Test
    void testClaimAuction() {
        String auctionUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e7";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(TestEntityFactory.getUserProfileDtoWithId(currentUserId));

        Assignment assignment = assignmentRepository.findAssignmentForPositionSlotAndUser(5L, auctionUserId).get();
        PositionSlotRequestDto requestDto = PositionSlotRequestDto.builder()
            .acceptedRewardPointsConfigHash("ABC")
            .build();
        AssignmentDto dto = positionSlotService.claimAuction(assignment.getPositionSlot().getId(), auctionUserId, currentUserId, requestDto);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), dto.getStatus());
    }

    @Test
    void testCancelAuction() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        // AssignedUser user = new AssignedUser(null, null, userId, null);
        // Mockito.when(userProviderMock.getCurrentUser()).thenReturn(user);

        Assignment auction = assignmentRepository.findAssignmentForPositionSlotAndUser(11L, userId).get();
        AssignmentDto dto = positionSlotService.cancelAuction(auction.getPositionSlot().getId(), userId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals((AssignmentStatus.ACCEPTED), dto.getStatus());
    }

    @Test
    void testLeave() throws ForbiddenException, NotFoundException {
        long positionSlotId = 1L;
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(TestEntityFactory.getUserProfileDtoWithId(userId));

        positionSlotService.leave(positionSlotId, userId);

        Assertions.assertFalse(assignmentRepository.findById(AssignmentId.of(positionSlotId, userId)).isPresent());
    }

    // TODO test join
    // TODO test join request
    // TODO test leave request

}
