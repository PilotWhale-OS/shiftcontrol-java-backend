package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
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
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    private final SecurityHelper securityHelper;

    @Override
    public PositionSlotDto findById(@NonNull Long positionSlotId) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot);

        return positionSlotAssemblingMapper.assemble(positionSlot);
    }

    @Override
    public AssignmentDto join(@NonNull Long positionSlotId, @NonNull String currentUserId) throws NotFoundException, ConflictException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        securityHelper.assertUserIsInPlan(positionSlot); // TODO is this correct?? Also: Shouldnt we assert here that user is not already assigned instead?
        securityHelper.assertUserIsNotAdmin(); // Admins cannot join shifts

        Volunteer volunteer = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found"));

        eligibilityService.validateSignUpStateForJoin(positionSlot, volunteer);
        eligibilityService.validateHasConflictingAssignments(
            currentUserId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
        //Todo: Implement actual joining logic

        // TODO close trades where this slot was offered to me

        //Todo: Send Eventbus event
        return null;
    }

    @Override
    public void leave(@NonNull Long positionSlotId, @NonNull String currentUserId) throws ForbiddenException, NotFoundException {

        //Todo: Checks are needed if the user can leave
        securityHelper.assertUserIsNotAdmin(); // Admins cannot leave shifts TODO: I think this can be removed since admins cannot join shifts in the first place

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
            throw new BadRequestException("assignment not up for auction");
        }
        // check if current user has access to the position slot (dont have to be volunteer itself, since planner should also be able to claim)
        securityHelper.assertUserIsInPlan(auction.getPositionSlot()); // TODO is this correct??
        securityHelper.assertUserIsNotAdmin(); // Admins cannot claim auctions

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

        return AssignmentMapper.toDto(assignmentDao.save(claimedAuction));
    }

    @Override
    public AssignmentDto cancelAuction(@NonNull Long positionSlotId, @NonNull String currentUserId) throws ForbiddenException {
        Assignment assignment = getAssignmentForUser(positionSlotId, currentUserId);

        // check if current user has access to the position slot (dont have to be volunteer itself, since planner should also be able to cancel in special scenarios)
        securityHelper.assertUserIsInPlan(assignment.getPositionSlot()); // TODO is this correct??
        securityHelper.assertUserIsNotAdmin(); // Admins cannot claim auctions

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
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
    public void setPreference(@NonNull String currentUserId, long positionSlotId, int preference) throws NotFoundException, ForbiddenException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot); // TODO is this correct??
        securityHelper.assertUserIsNotAdmin(); // Admins cannot set preferences


        if (preference < -10 || preference > 10) {
            throw new BadRequestException("preference must be between -10 and 10");
        }

        positionSlotDao.setPreference(currentUserId, positionSlotId, preference);
    }

    @Override
    public int getPreference(@NonNull String currentUserId, long positionSlotId) throws ForbiddenException, NotFoundException {
        PositionSlot positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));

        securityHelper.assertUserIsInPlan(positionSlot); // TODO is this correct??

        return positionSlotDao.getPreference(currentUserId, positionSlotId);
    }

    @Override
    public PositionSlotDto createPositionSlot(@NonNull Long shiftId, @NonNull PositionSlotModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        var shift = shiftDao.findById(shiftId)
            .orElseThrow(() -> new NotFoundException("Shift not found"));
        securityHelper.assertUserIsPlanner(shift);

        var newPositionSlot = PositionSlot.builder()
            .shift(shift)
            .build();

        validateModificationDtoAndSetPositionSlotFields(modificationDto, newPositionSlot);

        newPositionSlot = positionSlotDao.save(newPositionSlot);
        return positionSlotAssemblingMapper.assemble(newPositionSlot);
    }

    @Override
    public PositionSlotDto updatePositionSlot(@NonNull Long positionSlotId, @NonNull PositionSlotModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        securityHelper.assertUserIsPlanner(positionSlot);

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
    }
}
