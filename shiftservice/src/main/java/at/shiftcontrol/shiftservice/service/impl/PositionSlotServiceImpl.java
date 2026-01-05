package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
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
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.AssignmentEvent;
import at.shiftcontrol.shiftservice.event.events.PositionSlotEvent;
import at.shiftcontrol.shiftservice.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.shiftservice.event.events.PreferenceEvent;
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;
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
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public PositionSlotDto findById(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    @IsNotAdmin
    public AssignmentDto join(@NonNull Long positionSlotId, @NonNull String currentUserId) throws NotFoundException, ConflictException, ForbiddenException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        securityHelper.assertUserIsVolunteer(positionSlot);

        var volunteer = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            currentUserId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
        //Todo: Implement actual joining logic

        // TODO close trades where this slot was offered to me

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
            Map.of("positionSlotId", String.valueOf(positionSlotId),
                "volunteerId", currentUserId)),
            positionSlot, currentUserId));
        return null;
    }

    @Override
    @IsNotAdmin
    public void leave(@NonNull Long positionSlotId, @NonNull String volunteerId) throws ForbiddenException, NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        var volunteer = volunteerDao.findByUserId(volunteerId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        //Todo: Checks are needed if the user can leave

        // no security check necessary, because user is already assigned to position

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId));
    }

    @Override
    public Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return AssignmentMapper.toDto(positionSlot.getAssignments());
    }

    @Override
    @IsNotAdmin
    public AssignmentDto createAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        Assignment assignment = getAssignmentForUser(positionSlotId, currentUserId);
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

        publisher.publishEvent(AssignmentEvent.of(assignment,
            RoutingKeys.format(RoutingKeys.AUCTION_CREATED, Map.of("positionSlotId", String.valueOf(positionSlotId)))));
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    @Override
    @Transactional
    @IsNotAdmin
    public AssignmentDto claimAuction(@NonNull Long positionSlotId, @NonNull String offeringUserId, @NonNull String currentUserId)
        throws NotFoundException, ConflictException, ForbiddenException {
        // get auction-assignment
        Assignment auction = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, offeringUserId);
        if (auction == null
            || (auction.getStatus() != AssignmentStatus.AUCTION
            && auction.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN)) {
            throw new BadRequestException("assignment not up for auction");
        }
        // check if current user is volunteer in plan
        securityHelper.assertUserIsVolunteer(auction.getPositionSlot());

        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("user not found"));


        // check for trade not necessary

        // check if user is eligible for the auction position slot
        eligibilityService.validateSignUpStateForAuction(auction.getPositionSlot(), currentUser);

        // check if any current assignments overlap with auction position slot
        eligibilityService.validateHasConflictingAssignments(
            currentUser.getId(), auction.getPositionSlot());

        // cancel existing trades
        assignmentSwitchRequestDao.cancelTradesForAssignment(positionSlotId, offeringUserId);

        // execute claim
        Assignment claimedAuction = reassignAssignment(auction, currentUser);

        publisher.publishEvent(AssignmentEvent.of(claimedAuction,
            RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
                "positionSlotId", String.valueOf(positionSlotId),
                "volunteerId", currentUserId))));
        return AssignmentMapper.toDto(assignmentDao.save(claimedAuction));
    }

    @Override
    public AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String userId) throws ForbiddenException {
        Assignment assignment = getAssignmentForUser(positionSlotId, userId);

        if (!assignment.getAssignedVolunteer().getId().equals(userId)) {
            securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        } else {
            securityHelper.assertUserIsVolunteer(assignment.getPositionSlot());
        }


        assignment.setStatus(AssignmentStatus.ACCEPTED);

        assignment = assignmentDao.save(assignment);

        publisher.publishEvent(AssignmentEvent.of(assignment,
            RoutingKeys.format(RoutingKeys.AUCTION_CANCELED, Map.of("positionSlotId", String.valueOf(positionSlotId)))));
        return AssignmentMapper.toDto(assignment);
    }

    private Assignment getAssignmentForUser(Long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        if (assignment == null) {
            throw new BadRequestException("Not assigned to position slot");
        }
        return assignment;
    }

    private Assignment reassignAssignment(Assignment oldAssignment, Volunteer newVolunteer) {
        // Create new assignment with new PK
        Assignment newAssignment = AssignmentMapper.shallowCopy(oldAssignment);
        newAssignment.setId(new AssignmentId(oldAssignment.getPositionSlot().getId(), newVolunteer.getId()));
        newAssignment.setStatus(AssignmentStatus.ACCEPTED);
        newAssignment.setAssignedVolunteer(newVolunteer);

        assignmentDao.save(newAssignment);

        // Reassign dependent switch requests
        newAssignment.getIncomingSwitchRequests()
            .forEach(req -> req.setRequestedAssignment(newAssignment));
        newAssignment.getOutgoingSwitchRequests()
            .forEach(req -> req.setOfferingAssignment(newAssignment));
        assignmentSwitchRequestDao.saveAll(oldAssignment.getIncomingSwitchRequests());
        assignmentSwitchRequestDao.saveAll(oldAssignment.getOutgoingSwitchRequests());

        // Delete old assignment
        assignmentDao.delete(oldAssignment);

        return newAssignment;
    }

    @Override
    @IsNotAdmin
    public void setPreference(@NonNull String currentUserId, long positionSlotId, int preference) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsVolunteer(positionSlot);


        if (preference < -10 || preference > 10) {
            throw new BadRequestException("preference must be between -10 and 10");
        }

        positionSlotDao.setPreference(currentUserId, positionSlotId, preference);

        publisher.publishEvent(PreferenceEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_PREFERENCE_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlotId),
                "volunteerId", currentUserId)), currentUserId, preference, positionSlot));
    }

    @Override
    public int getPreference(@NonNull String currentUserId, long positionSlotId) throws ForbiddenException, NotFoundException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return positionSlotDao.getPreference(currentUserId, positionSlotId);
    }

    @Override
    public PositionSlotDto createPositionSlot(@NonNull Long shiftId, @NonNull PositionSlotModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        var shift = shiftDao.findById(shiftId)
            .orElseThrow(() -> new NotFoundException("Shift not found"));
        securityHelper.assertUserIsPlanner(shift);

        var positionSlot = PositionSlot.builder()
            .shift(shift)
            .build();

        validateModificationDtoAndSetPositionSlotFields(modificationDto, positionSlot);

        positionSlot = positionSlotDao.save(positionSlot);

        publisher.publishEvent(PositionSlotEvent.of(positionSlot, RoutingKeys.POSITIONSLOT_CREATED));
        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    public PositionSlotDto updatePositionSlot(@NonNull Long positionSlotId, @NonNull PositionSlotModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        securityHelper.assertUserIsPlanner(positionSlot);

        validateModificationDtoAndSetPositionSlotFields(modificationDto, positionSlot);

        positionSlot = positionSlotDao.save(positionSlot);

        publisher.publishEvent(PositionSlotEvent.of(positionSlot, RoutingKeys.format(RoutingKeys.POSITIONSLOT_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlotId)))));
        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    private void validateModificationDtoAndSetPositionSlotFields(PositionSlotModificationDto modificationDto, PositionSlot positionSlot)
        throws NotFoundException {
        if (modificationDto == null) {
            throw new BadRequestException("Modification data must be provided");
        }

        positionSlot.setName(modificationDto.getName());
        positionSlot.setDescription(modificationDto.getDescription());

        positionSlot.setSkipAutoAssignment(modificationDto.isSkipAutoAssignment());
        if (StringUtils.isNotBlank(modificationDto.getRoleId())) {
            var role = roleDao.findById(ConvertUtil.idToLong(modificationDto.getRoleId()))
                .orElseThrow(() -> new NotFoundException("Role not found"));
            positionSlot.setRole(role);
        } else {
            positionSlot.setRole(null);
        }
        positionSlot.setDesiredVolunteerCount(modificationDto.getDesiredVolunteerCount());
        positionSlot.setRewardPoints(modificationDto.getRewardPoints());
    }

    @Override
    public void deletePositionSlot(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        securityHelper.assertUserIsPlanner(positionSlot);

        positionSlotDao.delete(positionSlot);
        publisher.publishEvent(PositionSlotEvent.of(positionSlot, RoutingKeys.format(RoutingKeys.POSITIONSLOT_DELETED,
            Map.of("positionSlotId", String.valueOf(positionSlotId)))));
    }
}
