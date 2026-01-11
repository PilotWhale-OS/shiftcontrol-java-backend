package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@RequiredArgsConstructor
@Component
public class AssignmentSwitchRequestDaoImpl implements AssignmentSwitchRequestDao {
    private final AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Override
    public @NonNull String getName() {
        return "AssignmentSwitchRequest";
    }

    @Override
    public @NonNull Optional<AssignmentSwitchRequest> findById(AssignmentSwitchRequestId id) {
        return assignmentSwitchRequestRepository.findById(id);
    }

    @Override
    public AssignmentSwitchRequest save(AssignmentSwitchRequest entity) {
        return assignmentSwitchRequestRepository.save(entity);
    }

    @Override
    public Collection<AssignmentSwitchRequest> saveAll(Collection<AssignmentSwitchRequest> entities) {
        return assignmentSwitchRequestRepository.saveAll(entities);
    }

    @Override
    public void delete(AssignmentSwitchRequest entity) {
        assignmentSwitchRequestRepository.delete(entity);
    }

    @Override
    public void cancelTradesForAssignment(Long positionSlotId, String assignedUser) {
        assignmentSwitchRequestRepository.cancelTradesForAssignment(positionSlotId, assignedUser, TradeStatus.CANCELED);
    }

    @Override
    public Collection<AssignmentSwitchRequest> findOpenTradesForRequestedPositionAndOfferingUser(long positionSlotId, String userId) {
        return assignmentSwitchRequestRepository.findOpenTradesForRequestedPositionAndOfferingUser(positionSlotId, userId, TradeStatus.OPEN);
    }

    @Override
    public Collection<AssignmentSwitchRequest> findTradesForShiftPlanAndUser(long shiftPlanId, String userId) {
        return assignmentSwitchRequestRepository.findTradesForShiftPlanAndUser(shiftPlanId, userId);
    }

    @Override
    public void cancelTradesForOfferedPositionAndRequestedUser(long positionSlotId, String userId) {
        assignmentSwitchRequestRepository.cancelTradesForOfferedPositionAndRequestedUser(positionSlotId, userId, TradeStatus.CANCELED);
    }
}
