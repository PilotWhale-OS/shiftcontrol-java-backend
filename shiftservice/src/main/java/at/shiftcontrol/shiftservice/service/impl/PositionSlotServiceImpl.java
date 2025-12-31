package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
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
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public PositionSlotDto findById(@NonNull Long id) throws NotFoundException {
        return positionSlotDao.findById(id)
            .map(positionSlotAssemblingMapper::assemble)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
    }

    @Override
    public AssignmentDto join(@NonNull Long positionSlotId, @NonNull String volunteerId) throws NotFoundException, ConflictException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        var volunteer = volunteerDao.findByUserId(volunteerId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            volunteerId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
        //Todo: Implement actual joining logic

        //Todo: Send Eventbus event
        return null;
    }

    @Override
    public void leave(@NonNull Long positionSlotId, @NonNull Long volunteerId) {
        //Todo: Checks are needed if the user can leave
    }

    @Override
    public Collection<AssignmentDto> getAssignments(@NonNull Long positionSlotId) throws NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        return AssignmentMapper.toDto(positionSlot.getAssignments());
    }

    @Override
    public AssignmentDto createAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        Assignment assignment = getAssignmentForUser(positionSlotId, currentUserId);
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
        throws NotFoundException, ConflictException {
        // get auction-assignment
        Assignment auction = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, offeringUserId);
        if (auction == null
            || (auction.getStatus() != AssignmentStatus.AUCTION
            && auction.getStatus() != AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN)) {
            throw new BadRequestException("assignment not up for auction");
        }

        // get current user (volunteer)
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
    public AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) {
        Assignment assignment = getAssignmentForUser(positionSlotId, currentUserId);
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    private Assignment getAssignmentForUser(Long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        if (assignment == null) {
            throw new BadRequestException("not assigned to position slot");
        }
        return assignment;
    }

    @Transactional
    public Assignment reassignAssignment(Assignment oldAssignment, Volunteer newVolunteer) {
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
    public void setPreference(@NonNull String volunteerId, long positionSlotId, int preference) {
        if (preference < -10 || preference > 10) {
            throw new BadRequestException("preference must be between -10 and 10");
        }

        positionSlotDao.setPreference(volunteerId, positionSlotId, preference);
    }

    @Override
    public int getPreference(@NonNull String volunteerId, long positionSlotId) {
        return positionSlotDao.getPreference(volunteerId, positionSlotId);
    }

    @Override
    public PositionSlotDto createPositionSlot(@NonNull Long shiftId, @NonNull PositionSlotModificationDto modificationDto) throws NotFoundException {
        // TODO check permissions

        var shift = shiftDao.findById(shiftId)
            .orElseThrow(() -> new NotFoundException("Shift not found"));

        var newPositionSlot = PositionSlot.builder()
            .shift(shift)
            .build();

        validateModificationDtoAndSetPositionSlotFields(modificationDto, newPositionSlot);

        newPositionSlot = positionSlotDao.save(newPositionSlot);
        return positionSlotAssemblingMapper.assemble(newPositionSlot);
    }

    @Override
    public PositionSlotDto updatePositionSlot(@NonNull Long positionSlotId, @NonNull PositionSlotModificationDto modificationDto) throws NotFoundException {
        // TODO check permissions

        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        validateModificationDtoAndSetPositionSlotFields(modificationDto, positionSlot);

        positionSlot = positionSlotDao.save(positionSlot);
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
        var role = roleDao.findById(ConvertUtil.idToLong(modificationDto.getRoleId()))
            .orElseThrow(() -> new NotFoundException("Role not found"));
        positionSlot.setRole(role);
        positionSlot.setDesiredVolunteerCount(modificationDto.getDesiredVolunteerCount());
        positionSlot.setRewardPoints(modificationDto.getRewardPoints());
    }

    @Override
    public void deletePositionSlot(@NonNull Long positionSlotId) throws NotFoundException {
        // TODO check permissions

        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        positionSlotDao.delete(positionSlot);
    }
}
