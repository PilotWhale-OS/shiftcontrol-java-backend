package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;

@RequiredArgsConstructor
@Component
public class AssignmentDaoImpl implements AssignmentDao {
    private final AssignmentRepository assignmentRepository;

    @Override
    public Optional<Assignment> findById(AssignmentId id) {
        return assignmentRepository.findById(id);
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
    public Assignment findAssignmentForPositionSlotAndUser(long positionSlotId, long userId) {
        return assignmentRepository.findAssignmentForPositionSlotAndUser(positionSlotId, userId);
    }

}
