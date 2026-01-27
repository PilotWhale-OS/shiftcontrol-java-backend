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

import at.shiftcontrol.lib.type.TradeStatus;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dto.trade.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@WithMockUser(authorities = "USER")
public class AssignmentSwitchRequestServiceTest {

    @Autowired
    AssignmentSwitchRequestServiceImpl assignmentSwitchRequestService;
    @Autowired
    AssignmentDao assignmentDao;
    @Autowired
    AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;
    @Autowired
    TestEntityFactory testEntityFactory;

    @Autowired
    RewardPointsLedgerService rewardPointsLedgerService;

    @Autowired
    UserAttributeProvider attributeProvider;

    @MockitoBean
    SecurityHelper securityHelper;

    @MockitoBean
    KeycloakUserService keycloakUserService;

    @MockitoBean
    UserProfileService userProfileService;

    @MockitoBean
    ApplicationUserProvider applicationUserProvider;

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
        var currentUser = mock(ShiftControlUser.class);
        when(applicationUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getUserId()).thenReturn(currentUserId);
        Mockito.when(keycloakUserService.getUserById(any()))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(currentUserId));
        Mockito.when(keycloakUserService.getUserByIds(any()))
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId("28c02050-4f90-4f3a-b1df-3c7d27a166e7")));

        Collection<TradeCandidatesDto> result = assignmentSwitchRequestService.getPositionSlotsToOffer(positionSlotId, currentUserId);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertNotEquals(String.valueOf(positionSlotId), result.stream().findFirst().get().getOwnPosition().getId());
    }

    @Test
    void testCreateTrade() {
        assignmentSwitchRequestRepository.deleteById(1L);
        assignmentSwitchRequestRepository.flush();
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        String otherUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        String offeredPosition = "1";
        String requestedPosition = "2";
        Mockito.when(keycloakUserService.getUserById(currentUserId))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(currentUserId));
        Mockito.when(keycloakUserService.getUserById(otherUserId))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(otherUserId));

        TradeCreateDto createDto = TradeCreateDto.builder()
            .offeredPositionSlotId(offeredPosition)
            .requestedPositionSlotId(requestedPosition)
            .requestedVolunteerIds(List.of(otherUserId))
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
        Mockito.when(keycloakUserService.getUserById(any()))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(currentUserId));

        var pointsOtherUserBeforeTrade = rewardPointsLedgerService.getTotalPoints(otherUserId).getTotalPoints();
        var pointsCurrentUserBeforeTrade = rewardPointsLedgerService.getTotalPoints(currentUserId).getTotalPoints();
        TradeDto dto = assignmentSwitchRequestService.acceptTrade(1, currentUserId);
        var pointsOtherUserAfterTrade = rewardPointsLedgerService.getTotalPoints(otherUserId).getTotalPoints();
        var pointsCurrentUserAfterTrade = rewardPointsLedgerService.getTotalPoints(currentUserId).getTotalPoints();

        Assertions.assertNotNull(dto);

        Assertions.assertEquals(TradeStatus.ACCEPTED, dto.getStatus());

        // check if volunteers swapped (current user had the requested assignment, now has the offered one, and vice versa)
        Assertions.assertEquals(currentUserId, dto.getOfferingAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals(String.valueOf(offeredSlotId), dto.getOfferingAssignment().getPositionSlotId());
        // check if new assignment exists
        var newOffered = assignmentDao.findBySlotAndUser(offeredSlotId, currentUserId);
        Assertions.assertTrue(newOffered.isPresent());
        var newRequested = assignmentDao.findBySlotAndUser(requestedSlotId, otherUserId);
        Assertions.assertTrue(newRequested.isPresent());
        // old assignments should not exist anymore
        var deletedOffered = assignmentDao.findBySlotAndUser(offeredSlotId, otherUserId);
        Assertions.assertFalse(deletedOffered.isPresent());
        var deletedRequested = assignmentDao.findBySlotAndUser(requestedSlotId, currentUserId);
        Assertions.assertFalse(deletedRequested.isPresent());

        // check initial reward points before trade
        Assertions.assertEquals(20, pointsOtherUserBeforeTrade);
        Assertions.assertEquals(30, pointsCurrentUserBeforeTrade);
        // check reward points swapped after trade
        Assertions.assertEquals(40, pointsOtherUserAfterTrade);
        Assertions.assertEquals(10, pointsCurrentUserAfterTrade);
        // check reward points in dto stay the same for assignment (only volunteers swapped)
        Assertions.assertEquals(10, dto.getOfferingAssignment().getAcceptedRewardPoints());
        Assertions.assertEquals(30, dto.getRequestedAssignment().getAcceptedRewardPoints());
    }

    @Test
    void testDeclineTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e6";
        Mockito.when(keycloakUserService.getUserById(any()))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(currentUserId));

        TradeDto dto = assignmentSwitchRequestService.declineTrade(1, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.REJECTED, dto.getStatus());
        Assertions.assertEquals(currentUserId, dto.getRequestedAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals("2", dto.getRequestedAssignment().getPositionSlotId());
    }

    @Test
    void testCancelTrade() {
        String currentUserId = "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
        Mockito.when(keycloakUserService.getUserById(any()))
            .thenReturn(testEntityFactory.getUserRepresentationWithId(currentUserId));

        TradeDto dto = assignmentSwitchRequestService.cancelTrade(1, currentUserId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.CANCELED, dto.getStatus());
        Assertions.assertEquals(currentUserId, dto.getOfferingAssignment().getAssignedVolunteer().getId());
        Assertions.assertEquals("1", dto.getOfferingAssignment().getPositionSlotId());
    }
}
