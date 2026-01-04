package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.type.TradeStatus;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class AssignmentSwitchRequestServiceIT {

    @Autowired
    AssignmentSwitchRequestServiceImpl assignmentSwitchRequestService;

    @MockitoBean
    UserProfileService userProfileService;


    @Test
    void testGetPositionSlotsToOffer() throws NotFoundException, ForbiddenException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long positionSlotId = 3L;
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(getUserProfileDtoWithId("28c02050-4f90-4f3a-b1df-3c7d27a166e7"));

        Collection<TradeCandidatesDto> result = assignmentSwitchRequestService.getPositionSlotsToOffer(positionSlotId, currentUserId);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertNotEquals(String.valueOf(positionSlotId), result.stream().findFirst().get().getPositionSlotId());
    }

    @Test
    void testCreateTrade() throws ConflictException, NotFoundException, ForbiddenException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        String otherUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        String offeredPosition = "11";
        String requestedPosition = "12";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(getUserProfileDtoWithId(currentUserId));
        TradeCreateDto createDto = TradeCreateDto.builder()
            .offeredPositionSlotId(offeredPosition)
            .requestedPositionSlotId(requestedPosition)
            .requestedVolunteers(List.of(
                VolunteerDto.builder().id(otherUserId).build()))
            .build();

        Collection<TradeDto> dtos = assignmentSwitchRequestService.createTrade(createDto, currentUserId);

        Assertions.assertNotNull(dtos);
        Assertions.assertFalse(dtos.isEmpty());
        TradeDto dto = dtos.stream().findFirst().get();
        Assertions.assertEquals(TradeStatus.OPEN, dto.getStatus());
        Assertions.assertEquals(requestedPosition, dto.getRequestedAssignment().getPositionSlotId());
        Assertions.assertEquals(offeredPosition, dto.getOfferingAssignment().getPositionSlotId());
        Assertions.assertEquals(currentUserId, dto.getOfferingAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals(otherUserId, dto.getRequestedAssignment().getAssignedVolunteer().getId());
    }

    @Test
    void testAcceptTrade() throws ConflictException, NotFoundException, ForbiddenException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(getUserProfileDtoWithId(currentUserId));
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5"),
            new AssignmentId(2L, currentUserId)
        );

        TradeDto dto = assignmentSwitchRequestService.acceptTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.ACCEPTED, dto.getStatus());
    }

    @Test
    void testDeclineTrade() throws NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5"),
            new AssignmentId(2L, currentUserId)
        );

        TradeDto dto = assignmentSwitchRequestService.declineTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.REJECTED, dto.getStatus());
    }

    @Test
    void testCancelTrade() throws NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, currentUserId),
            new AssignmentId(2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6")
        );

        TradeDto dto = assignmentSwitchRequestService.cancelTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.CANCELED, dto.getStatus());
    }

    private UserProfileDto getUserProfileDtoWithId(String userId) {
        UserProfileDto profile = new UserProfileDto();
        AccountInfoDto info = new AccountInfoDto(
            userId,
            "Test Username",
            "first name",
            "last name",
            "mail@mail.com",
            UserType.ASSIGNED
        );
        profile.setAccount(info);
        return profile;
    }
}
