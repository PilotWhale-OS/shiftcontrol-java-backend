package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;

@RequiredArgsConstructor
@Service
public class PositionSlotServiceImpl implements PositionSlotService {
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;

    private final EligibilityService eligibilityService;
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;
    private final ApplicationUserProvider userProvider;

    @Override
    public PositionSlotDto findById(Long id) throws NotFoundException {
        return positionSlotDao.findById(id)
            .map(positionSlotAssemblingMapper::assemble)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
    }

    @Override
    public AssignmentDto join(Long positionSlotId, String userId) throws NotFoundException, ConflictException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        var volunteer = volunteerDao.findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            userId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
        //Todo: Implement actual joining logic

        //Todo: Send Eventbus event
        return null;
    }

    @Override
    public void leave(Long positionSlotId, Long userId) {
        //Todo: Checks are needed if the user can leave
    }

    @Override
    public Collection<AssignmentDto> getAssignments(Long positionSlotId) throws NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        return AssignmentMapper.toDto(positionSlot.getAssignments());
    }

    @Override
    public AssignmentDto createAuction(Long positionSlotId) {
        Assignment assignment = getAssignmentForCurrentUser(positionSlotId);
        // check for signup phase
        LockStatus lockStatus = assignment.getPositionSlot().getShift().getLockStatus();
        switch (lockStatus) {
            case SELF_ASSIGNABLE:
                throw new IllegalStateException("Auction not possible, unassign instead");
            case SELF_ASSIGNABLE_NO_UNASSIGN:
                // proceed with auction
                assignment.setStatus(AssignmentStatus.AUCTION);
                break;
            case LOCKED:
                throw new IllegalStateException("Auction not possible, shift is locked");
        }
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentDto claimAuction(Long positionSlotId, String offeringUserId) throws NotFoundException, ConflictException {
        // get auction-assignment
        Assignment auction = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, offeringUserId);
        if (auction == null
            || (auction.getStatus() != AssignmentStatus.AUCTION
            && auction.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN)) {
            throw new IllegalArgumentException("assignment not up for auction");
        }

        // get current user (volunteer)
        String currentUserId = userProvider.getCurrentUser().getUserId();
        Volunteer currentUser = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("user not found"));


        // check if volunteer has access to shift plan
        eligibilityService.validateHasAccessToPositionSlot(auction.getPositionSlot(), currentUserId);

        // check for position signup state
        eligibilityService.validateSignUpStateForAuction(auction.getPositionSlot(), currentUser);

        // check for trade not necessary

        // check if user is eligible for the auction position slot
        eligibilityService.validateSignUpStateForTrade(auction.getPositionSlot(), currentUser);

        // check if any current assignments overlap with auction position slot
        eligibilityService.validateHasConflictingAssignments(
            currentUser.getId(), auction.getPositionSlot().getShift().getStartTime(),
            auction.getPositionSlot().getShift().getEndTime());

        // cancel existing trades
        assignmentSwitchRequestDao.cancelTradesForAssignment(positionSlotId, offeringUserId);

        // execute claim
        Assignment claimedAuction = reassignAssignment(auction, currentUser);

        return AssignmentMapper.toDto(assignmentDao.save(claimedAuction));
    }

    @Override
    public AssignmentDto cancelAuction(Long positionSlotId) {
        Assignment assignment = getAssignmentForCurrentUser(positionSlotId);
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    private Assignment getAssignmentForCurrentUser(Long positionSlotId) {
        String userId = userProvider.getCurrentUser().getUserId();
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        if (assignment == null) {
            throw new IllegalArgumentException("not assigned to position slot");
        }
        return assignment;
    }

    @Transactional
    public Assignment reassignAssignment(Assignment oldAssignment, Volunteer newVolunteer) {

        // Create new assignment with new PK
        Assignment newAssignment = new Assignment();
        newAssignment.setId(new AssignmentId(oldAssignment.getPositionSlot().getId(), newVolunteer.getId()));
        newAssignment.setStatus(AssignmentStatus.ACCEPTED);
        newAssignment.setAssignedVolunteer(newVolunteer);
        newAssignment.setPositionSlot(oldAssignment.getPositionSlot());
        newAssignment.setIncomingSwitchRequests(oldAssignment.getIncomingSwitchRequests());
        newAssignment.setOutgoingSwitchRequests(oldAssignment.getOutgoingSwitchRequests());

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

}
