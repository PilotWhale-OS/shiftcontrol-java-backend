package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import config.TestSecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.entity.AssignmentId;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.type.TradeStatus;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dto.trade.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@WithMockUser(authorities = "USER")
public class AssignmentSwitchRequestServiceIT {

    @Autowired
    AssignmentSwitchRequestServiceImpl assignmentSwitchRequestService;
    @Autowired
    AssignmentDao assignmentDao;
    @Autowired
    TestEntityFactory testEntityFactory;

    @Autowired
    UserAttributeProvider attributeProvider;

    @MockitoBean
    SecurityHelper securityHelper;

    @MockitoBean
    UserProfileService userProfileService;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        setDefaultNonAdminTestUser();
    }

    private void setDefaultNonAdminTestUser() {
        ShiftControlUser principal = new AssignedUser(
            List.of(),
            TestSecurityConfig.HDR_USERNAME,
            TestSecurityConfig.HDR_USERID,
            attributeProvider
        );

        var auth = new UsernamePasswordAuthenticationToken(
            principal,
            "N/A",
            principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetPositionSlotsToOffer() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long positionSlotId = 3L;
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId("28c02050-4f90-4f3a-b1df-3c7d27a166e7"));

        Collection<TradeCandidatesDto> result = assignmentSwitchRequestService.getPositionSlotsToOffer(positionSlotId, currentUserId);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertNotEquals(String.valueOf(positionSlotId), result.stream().findFirst().get().getPositionSlotId());
    }

    @Test
    void testCreateTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        String otherUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        String offeredPosition = "11";
        String requestedPosition = "12";
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(currentUserId));
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
    void testAcceptTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        String otherUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        long offeredSlotId = 1L;
        long requestedSlotId = 2L;
        Mockito.when(userProfileService.getUserProfile(any()))
            .thenReturn(testEntityFactory.getUserProfileDtoWithId(currentUserId));
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(offeredSlotId, otherUserId),
            new AssignmentId(requestedSlotId, currentUserId)
        );

        TradeDto dto = assignmentSwitchRequestService.acceptTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.ACCEPTED, dto.getStatus());
        Assertions.assertEquals(currentUserId, dto.getOfferingAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(offeredSlotId), dto.getOfferingAssignment().getPositionSlotId());
        // check if new assignment exists
        var newOffered = assignmentDao.findById(AssignmentId.of(offeredSlotId, currentUserId));
        Assertions.assertTrue(newOffered.isPresent());
        var newRequested = assignmentDao.findById(AssignmentId.of(requestedSlotId, otherUserId));
        Assertions.assertTrue(newRequested.isPresent());
        // old assignments should not exist anymore
        var deletedOffered = assignmentDao.findById(AssignmentId.of(offeredSlotId, otherUserId));
        Assertions.assertFalse(deletedOffered.isPresent());
        var deletedRequested = assignmentDao.findById(AssignmentId.of(requestedSlotId, currentUserId));
        Assertions.assertFalse(deletedRequested.isPresent());
    }

    @Test
    void testDeclineTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "28c02050-4f90-4f3a-b1df-3c7d27a166e5"),
            new AssignmentId(2L, currentUserId)
        );

        TradeDto dto = assignmentSwitchRequestService.declineTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.REJECTED, dto.getStatus());
        Assertions.assertEquals(currentUserId, dto.getRequestedAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals("2", dto.getRequestedAssignment().getPositionSlotId());
    }

    @Test
    void testCancelTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, currentUserId),
            new AssignmentId(2L, "28c02050-4f90-4f3a-b1df-3c7d27a166e6")
        );

        TradeDto dto = assignmentSwitchRequestService.cancelTrade(id, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.CANCELED, dto.getStatus());
        Assertions.assertEquals(currentUserId, dto.getOfferingAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals("1", dto.getOfferingAssignment().getPositionSlotId());
    }
}
