package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
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
    private final VolunteerDao volunteerDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;

    private final EligibilityService eligibilityService;
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;
    private final SecurityHelper securityHelper;

    @Override
    public PositionSlotDto findById(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    public AssignmentDto join(@NonNull Long positionSlotId, @NonNull String volunteerId) throws NotFoundException, ConflictException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        Volunteer volunteer = volunteerDao.findByUserId(volunteerId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        securityHelper.assertUserIsVolunteer(positionSlot, volunteer);

        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            volunteerId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
        //Todo: Implement actual joining logic

        // TODO close trades where this slot was offered to me

        //Todo: Send Eventbus event
        return null;
    }

    @Override
    public void leave(@NonNull Long positionSlotId, @NonNull String volunteerId) throws ForbiddenException, NotFoundException {

        //Todo: Checks are needed if the user can leave

        // no security check necessary, because user is already assigned to position
    }

    @Override
    public Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return AssignmentMapper.toDto(positionSlot.getAssignments());
    }

    @Override
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
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentDto claimAuction(@NonNull Long positionSlotId, @NonNull String offeringUserId, @NonNull String currentUserId)
        throws NotFoundException, ConflictException, ForbiddenException {

        // get auction-assignment
        Assignment auction = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, offeringUserId);
        if (auction == null
            || (auction.getStatus() != AssignmentStatus.AUCTION
            && auction.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN)) {
            throw new IllegalArgumentException("assignment not up for auction");
        }

        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("user not found"));

        // check if volunteer has access to shift plan
        securityHelper.assertUserIsVolunteer(auction.getPositionSlot(), currentUser);

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

        return AssignmentMapper.toDto(assignmentDao.save(claimedAuction));
    }

    @Override
    public AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) throws ForbiddenException {
        Assignment assignment = getAssignmentForUser(positionSlotId, currentUserId);

        securityHelper.assertUserIsVolunteer(assignment.getPositionSlot(), currentUserId);

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    private Assignment getAssignmentForUser(Long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        if (assignment == null) {
            throw new IllegalArgumentException("not assigned to position slot");
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
    public void setPreference(@NonNull String volunteerId, long positionSlotId, int preference) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsVolunteer(positionSlot, volunteerId);

        if (preference < -10 || preference > 10) {
            throw new BadRequestException("preference must be between -10 and 10");
        }

        positionSlotDao.setPreference(volunteerId, positionSlotId, preference);
    }

    @Override
    public int getPreference(@NonNull String volunteerId, long positionSlotId) throws ForbiddenException, NotFoundException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsVolunteer(positionSlot, volunteerId);

        return positionSlotDao.getPreference(volunteerId, positionSlotId);
    }
}
