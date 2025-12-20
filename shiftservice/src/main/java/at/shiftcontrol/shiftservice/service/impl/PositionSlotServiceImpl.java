package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.assembler.PositionSlotDtoAssembler;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@RequiredArgsConstructor
@Service
public class PositionSlotServiceImpl implements PositionSlotService {
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;
    private final AssignmentDao assignmentDao;

    private final EligibilityService eligibilityService;
    private final PositionSlotDtoAssembler positionSlotDtoAssembler;

    @Override
    public PositionSlotDto findById(Long id) throws NotFoundException {
        return positionSlotDao.findById(id)
            .map(positionSlotDtoAssembler::assemble)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
    }

    @Override
    public AssignmentDto join(Long positionSlotId, Long userId) throws NotFoundException, ConflictException {
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
        return AssignmentMapper.toDto(getAssignmentEntites(positionSlotId));
    }

    @Override
    public AssignmentDto auction(Long positionSlotId) throws NotFoundException {
        var assignments = getAssignmentEntites(positionSlotId);

        // get users assignment
        // TODO use current user
        Assignment assignment = assignments.stream().filter(a -> a.getAssignedVolunteer().getId() == 1L).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("not assigned to position slot"));

        //Todo: Checks are needed if the user can auction
        assignment.setStatus(AssignmentStatus.AUCTION);
        return AssignmentMapper.toDto(assignmentDao.save(assignment));
    }

    private Collection<Assignment> getAssignmentEntites(Long positionSlotId) throws NotFoundException {
        var positionSlot = positionSlotDao.findById(positionSlotId)
            .orElseThrow(() -> new NotFoundException("PositionSlot not found"));
        return positionSlot.getAssignments();
    }
}
