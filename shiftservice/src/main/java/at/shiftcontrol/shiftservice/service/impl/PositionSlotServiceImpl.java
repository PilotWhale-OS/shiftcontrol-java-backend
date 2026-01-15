package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.PositionSlotEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.PreferenceEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotModificationDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.mapper.AssignmentAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.util.LockStatusHelper;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@RequiredArgsConstructor
@Service
public class PositionSlotServiceImpl implements PositionSlotService {
    private final PositionSlotDao positionSlotDao;
    private final ShiftDao shiftDao;
    private final RoleDao roleDao;
    private final VolunteerDao volunteerDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;

    private final EligibilityService eligibilityService;
    private final AssignmentService assignmentService;
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;

    @Override
    public PositionSlotDto findById(@NonNull Long positionSlotId) {
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsInPlan(positionSlot);
        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    @Transactional
    @IsNotAdmin
    public AssignmentDto join(@NonNull Long positionSlotId, @NonNull String currentUserId, @NonNull PositionSlotRequestDto requestDto) {
        // get position slot and volunteer
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        Volunteer volunteer = volunteerDao.getById(currentUserId);

        // check if plan is locked or supervised
        LockStatusHelper.assertIsSelfSignUpWithMessage(positionSlot, "join");

        // check access, already assigned, eligible and conflicts
        assertJoinPossible(positionSlot, volunteer);

        // assign volunteer to slot
        Assignment assignment = assignmentService.assign(positionSlot, volunteer, requestDto);

        // save and return
        return assignmentAssemblingMapper.toDto(assignmentDao.save(assignment));
    }

    @Override
    @Transactional
    @IsNotAdmin
    public void leave(@NonNull Long positionSlotId, @NonNull String volunteerId) {
        // get assignment
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, volunteerId);

        // check if plan is locked or supervised
        LockStatusHelper.assertIsSelfSignUpWithMessage(assignment, "leave");

        // leave
        assignmentService.unassign(assignment);
    }

    @Override
    @IsNotAdmin
    public AssignmentDto joinRequest(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        // get position slot and volunteer
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        Volunteer volunteer = volunteerDao.getById(currentUserId);

        // check if plan is supervised
        LockStatusHelper.assertIsSupervisedWithMessage(positionSlot, "join request");

        // check access, already assigned, eligible and conflicts
        assertJoinPossible(positionSlot, volunteer);

        // create assignment
        Assignment joinRequest = Assignment.of(positionSlot, volunteer, AssignmentStatus.REQUEST_FOR_ASSIGNMENT);

        // publish event
        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", currentUserId)),
            positionSlot, currentUserId));

        return assignmentAssemblingMapper.toDto(assignmentDao.save(joinRequest));
    }

    @Override
    @IsNotAdmin
    public void leaveRequest(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        // get assignment
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, currentUserId);

        // check if plan is locked or supervised
        LockStatusHelper.assertIsSupervisedWithMessage(assignment, "leave request");

        // update assignment
        assignment.setStatus(AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN);

        // publish event
        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", currentUserId)),
            assignment.getPositionSlot(), currentUserId));

        assignmentDao.save(assignment);
    }

    @Override
    @IsNotAdmin
    public void joinRequestWithdraw(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        // get assignment
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, currentUserId);
        // check status
        if (assignment.getStatus() != AssignmentStatus.REQUEST_FOR_ASSIGNMENT) {
            throw new IllegalArgumentException("Assignment not in request status");
        }
        LockStatusHelper.assertIsSupervisedWithMessage(assignment, "withdraw join request");
        // delete assignment
        assignmentDao.delete(assignment);
        // publish event
        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_WITHDRAW,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", currentUserId)),
            assignment.getPositionSlot(), currentUserId));
    }

    @Override
    @IsNotAdmin
    public void leaveRequestWithdraw(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        // get assignment
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, currentUserId);
        // check status
        if (assignment.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN) {
            throw new IllegalArgumentException("Assignment not in request status");
        }
        LockStatusHelper.assertIsSupervisedWithMessage(assignment, "withdraw leave request");
        // change back to signed-up state
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentDao.save(assignment);
        // publish event
        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", currentUserId)),
            assignment.getPositionSlot(), currentUserId));
    }

    private void assertJoinPossible(PositionSlot positionSlot, Volunteer volunteer) {
        // check access to position slot
        securityHelper.assertUserIsVolunteer(positionSlot, true);

        // check if already assigned, eligible and conflicts
        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            volunteer.getId(), positionSlot);
    }

    @Override
    public Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) {
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsInPlan(positionSlot);
        return assignmentAssemblingMapper.toDto(positionSlot.getAssignments());
    }

    @Override
    public AssignmentDto getUserAssignment(@NonNull Long positionSlotId, @NonNull String volunteerId) {
        return assignmentAssemblingMapper.toDto(assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, volunteerId));
    }

    @Override
    @IsNotAdmin
    public AssignmentDto createAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, currentUserId);
        // no security check necessary, because user is already assigned to position,
        //  if not, assignment would not be found
        // check for signup phase
        LockStatus lockStatus = assignment.getPositionSlot().getShift().getShiftPlan().getLockStatus();
        switch (lockStatus) {
            case SELF_SIGNUP:
                throw new IllegalStateException("Auction not possible, unassign instead");
            case SUPERVISED:
                // proceed with auction
                assignment.setStatus(AssignmentStatus.AUCTION);
                break;
            case LOCKED:
                throw new IllegalStateException("Auction not possible, shift is locked");
            default:
                throw new IllegalStateException("Unexpected value: " + lockStatus);
        }

        publisher.publishEvent(AssignmentEvent.of(
            RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                Map.of("positionSlotId", String.valueOf(positionSlotId))
            ), assignment
        ));
        return assignmentAssemblingMapper.toDto(assignmentDao.save(assignment));
    }

    @Override
    @Transactional
    @IsNotAdmin
    public AssignmentDto claimAuction(@NonNull Long positionSlotId, @NonNull String offeringUserId, @NonNull String currentUserId,
                                      @NonNull PositionSlotRequestDto requestDto) {
        // get auction-assignment
        Assignment auction = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, offeringUserId);
        if (auction.getStatus() != AssignmentStatus.AUCTION
            && auction.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN) {
            throw new BadRequestException("assignment not up for auction");
        }
        // check if current user is volunteer in plan
        securityHelper.assertUserIsVolunteer(auction.getPositionSlot(), true);
        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.getById(currentUserId);
        // check for trade not necessary
        // check if user is eligible for the auction position slot
        eligibilityService.validateSignUpStateForAuction(auction.getPositionSlot(), currentUser);
        // check if any current assignments overlap with auction position slot
        eligibilityService.validateHasConflictingAssignments(
            currentUser.getId(), auction.getPositionSlot());
        // cancel existing trades
        assignmentSwitchRequestDao.cancelTradesForAssignment(positionSlotId, offeringUserId);
        // execute claim
        Assignment claimedAuction = assignmentService.claimAuction(auction, currentUser, requestDto);

        return assignmentAssemblingMapper.toDto(claimedAuction);
    }

    @Override
    public AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String userId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, userId);
        if (!assignment.getAssignedVolunteer().getId().equals(userId)) {
            securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        } else {
            securityHelper.assertUserIsVolunteer(assignment.getPositionSlot(), true);
        }
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment = assignmentDao.save(assignment);
        publisher.publishEvent(AssignmentEvent.of(
            RoutingKeys.format(
                RoutingKeys.AUCTION_CANCELED,
                Map.of("positionSlotId", String.valueOf(positionSlotId))
            ), assignment
        ));
        return assignmentAssemblingMapper.toDto(assignment);
    }

    @Override
    @IsNotAdmin
    public void setPreference(@NonNull String currentUserId, long positionSlotId, int preference) {
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsVolunteer(positionSlot, true);
        if (preference < -10 || preference > 10) {
            throw new BadRequestException("preference must be between -10 and 10");
        }
        positionSlotDao.setPreference(currentUserId, positionSlotId, preference);
        publisher.publishEvent(PreferenceEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_PREFERENCE_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlotId),
                "volunteerId", currentUserId)), currentUserId, preference, positionSlot));
    }

    @Override
    public int getPreference(@NonNull String currentUserId, long positionSlotId) {
        PositionSlot positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsInPlan(positionSlot);
        return positionSlotDao.getPreference(currentUserId, positionSlotId);
    }

    @Override
    public PositionSlotDto createPositionSlot(@NonNull Long shiftId, @NonNull PositionSlotModificationDto modificationDto) {
        var shift = shiftDao.getById(shiftId);
        securityHelper.assertUserIsPlanner(shift);
        var positionSlot = PositionSlot.builder()
            .shift(shift)
            .build();
        validateModificationDtoAndSetPositionSlotFields(modificationDto, positionSlot);
        positionSlot = positionSlotDao.save(positionSlot);
        publisher.publishEvent(PositionSlotEvent.of(RoutingKeys.POSITIONSLOT_CREATED, positionSlot));
        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    public PositionSlotDto updatePositionSlot(@NonNull Long positionSlotId, @NonNull PositionSlotModificationDto modificationDto) {
        var positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsPlanner(positionSlot);
        validateModificationDtoAndSetPositionSlotFields(modificationDto, positionSlot);
        positionSlot = positionSlotDao.save(positionSlot);
        publisher.publishEvent(PositionSlotEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlotId))), positionSlot));
        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    private void validateModificationDtoAndSetPositionSlotFields(PositionSlotModificationDto modificationDto, PositionSlot positionSlot) {
        if (modificationDto == null) {
            throw new BadRequestException("Modification data must be provided");
        }
        positionSlot.setName(modificationDto.getName());
        positionSlot.setDescription(modificationDto.getDescription());
        positionSlot.setSkipAutoAssignment(modificationDto.isSkipAutoAssignment());
        if (StringUtils.isNotBlank(modificationDto.getRoleId())) {
            var role = roleDao.getById(ConvertUtil.idToLong(modificationDto.getRoleId()));
            positionSlot.setRole(role);
        } else {
            positionSlot.setRole(null);
        }
        positionSlot.setDesiredVolunteerCount(modificationDto.getDesiredVolunteerCount());
        positionSlot.setOverrideRewardPoints(modificationDto.getOverrideRewardPoints());
    }

    @Override
    public void deletePositionSlot(@NonNull Long positionSlotId) {
        var positionSlot = positionSlotDao.getById(positionSlotId);
        securityHelper.assertUserIsPlanner(positionSlot);
        positionSlotDao.delete(positionSlot);
        publisher.publishEvent(PositionSlotEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_DELETED,
            Map.of("positionSlotId", String.valueOf(positionSlotId))), positionSlot));
    }
}
