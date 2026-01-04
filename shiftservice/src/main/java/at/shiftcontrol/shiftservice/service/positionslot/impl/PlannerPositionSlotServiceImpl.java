package at.shiftcontrol.shiftservice.service.positionslot.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.mapper.AssignmentRequestMapper;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class PlannerPositionSlotServiceImpl implements PlannerPositionSlotService {
    private final SecurityHelper securityHelper;
    private final ShiftPlanDao shiftPlanDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentRepository assignmentRepository;

    @Override
    public Collection<AssignmentRequestDto> getSlots(long shiftPlanId, AssignmentFilterDto filterDto) throws ForbiddenException, NotFoundException {
        var plan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(plan);
        return AssignmentRequestMapper.toAssignmentRequestDto(plan.getShifts());
    }

    @Override
    public void acceptRequest(long shiftPlanId, long positionSlotId, String userId) throws ForbiddenException {
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not acceptable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> assignmentDao.delete(assignment);
            case REQUEST_FOR_ASSIGNMENT -> acceptAssignment(assignment);
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        //TODO publish event
    }

    @Override
    public void declineRequest(long shiftPlanId, long positionSlotId, String userId) throws ForbiddenException {
        Assignment assignment = assignmentDao.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not declineable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> acceptAssignment(assignment);
            case REQUEST_FOR_ASSIGNMENT -> assignmentDao.delete(assignment);
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        //TODO publish event
    }

    private void acceptAssignment(Assignment assignment) {
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentRepository.save(assignment);
    }
}
