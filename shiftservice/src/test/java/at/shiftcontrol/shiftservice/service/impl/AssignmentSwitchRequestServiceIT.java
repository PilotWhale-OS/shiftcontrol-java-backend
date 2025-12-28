package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.exception.ConflictException;
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
    // TODO update tests after implementation

    @Autowired
    AssignmentSwitchRequestServiceImpl assignmentSwitchRequestService;

    @MockitoBean
    UserProfileService userProfileService;

    @Test
    @Disabled
    void testCreateTrade() throws ConflictException, NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        TradeCreateDto createDto = TradeCreateDto.builder()
            .offeredPositionSlotId("1")
            .requestedPositionSlotId("3")
            .requestedVolunteers(List.of(VolunteerDto.builder().id("3").build()))
            .build();

        Collection<TradeDto> dtos = assignmentSwitchRequestService.createTrade(createDto, currentUserId);

        Assertions.assertNotNull(dtos);
        Assertions.assertFalse(dtos.isEmpty());
        dtos.forEach(trade -> Assertions.assertEquals(TradeStatus.OPEN, trade.getStatus()));
    }

    @Test
    void testGetPositionSlotsToOffer() throws NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long positionSlotId = 3L;
        Mockito.when(userProfileService.getUserProfile(any())).thenReturn(getUserProfileDto());

        Collection<TradeCandidatesDto> result = assignmentSwitchRequestService.getPositionSlotsToOffer(positionSlotId, currentUserId);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertNotEquals(String.valueOf(positionSlotId), result.stream().findFirst().get().getPositionSlotId());
    }

    @Test
    @Disabled
    void testAcceptTrade() throws ConflictException, NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = assignmentSwitchRequestService.acceptTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.ACCEPTED, dto.getStatus());
    }

    @Test
    @Disabled
    void testDeclineTrade() throws NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = assignmentSwitchRequestService.declineTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.REJECTED, dto.getStatus());
    }

    @Test
    @Disabled
    void testCancelTrade() throws NotFoundException {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = assignmentSwitchRequestService.cancelTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.CANCELED, dto.getStatus());
    }

    private UserProfileDto getUserProfileDto() {
        UserProfileDto profile = new UserProfileDto();
        AccountInfoDto info = new AccountInfoDto(
            "userId",
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
