package at.shiftcontrol.shiftservice.dao.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.type.AssignmentStatus;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;

@RequiredArgsConstructor
@Component
public class AssignmentDaoImpl implements AssignmentDao {
    private final AssignmentRepository assignmentRepository;

    @Override
    public @NonNull String getName() {
        return "Assignment";
    }

    @Override
    public @lombok.NonNull Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }

    public Optional<Assignment> findBySlotAndUser(long positionSlotId, String assignedUserId) {
        return assignmentRepository.findBySlotAndUser(positionSlotId, assignedUserId);
    }

    @Override
    public Assignment save(Assignment entity) {
        return assignmentRepository.save(entity);
    }

    @Override
    public Collection<Assignment> saveAll(Collection<Assignment> entities) {
        return assignmentRepository.saveAll(entities);
    }

    @Override
    public void delete(Assignment entity) {
        assignmentRepository.delete(entity);
    }

    @Override
    public Collection<Assignment> findAuctionsByShiftPlanId(long shiftPlanId) {
        return assignmentRepository.findAuctionsByShiftPlanId(shiftPlanId);
    }

    @Override
    public Collection<Assignment> findAuctionsByShiftPlanIdExcludingUser(long shiftPlanId, String userId) {
        return assignmentRepository.findAuctionsByShiftPlanIdExcludingUser(shiftPlanId, userId);
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) {
        return assignmentRepository.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public Collection<Assignment> getActiveAssignmentsOfSlot(long positionSlotId) {
        return assignmentRepository.getAssignmentsOfSlotNotInState(positionSlotId, AssignmentStatus.REQUEST_FOR_ASSIGNMENT);
    }

    @Override
    public Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlotId) {
        return assignmentRepository.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlotId);
    }

    @Override
    public Assignment getAssignmentForPositionSlotAndUser(long positionSlotId, String userId) {
        return assignmentRepository.findAssignmentForPositionSlotAndUser(positionSlotId, userId)
            .orElseThrow(() -> new NotFoundException("Not assigned to position slot."));
    }

    @Override
    public Collection<Assignment> getAssignmentForPositionSlotAndUsers(long positionSlotId, Collection<String> userIds) {
        return assignmentRepository.findAssignmentForPositionSlotAndUsers(positionSlotId, userIds);
    }

    @Override
    public Collection<Assignment> findAssignmentsForShiftPlanAndUser(long shiftPlanId, String userId) {
        return assignmentRepository.findAssignmentsForShiftPlanAndUser(shiftPlanId, userId);
    }

    @Override
    public void deleteAll(Collection<Assignment> ids) {
        assignmentRepository.deleteAll(ids);
    }
}
