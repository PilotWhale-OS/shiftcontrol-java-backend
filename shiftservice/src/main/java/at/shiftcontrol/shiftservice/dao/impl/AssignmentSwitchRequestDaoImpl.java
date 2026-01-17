package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.type.TradeStatus;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;

@RequiredArgsConstructor
@Component
public class AssignmentSwitchRequestDaoImpl implements AssignmentSwitchRequestDao {
    private final AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Override
    public @NonNull String getName() {
        return "AssignmentSwitchRequest";
    }

    @Override
    public @NonNull Optional<AssignmentSwitchRequest> findById(Long id) {
        return assignmentSwitchRequestRepository.findById(id);
    }

    @Override
    public Optional<AssignmentSwitchRequest> findByAssignmentIds(long offeredAssignmentId, long requestedAssignmentId) {
        return assignmentSwitchRequestRepository.findByAssignmentIds(offeredAssignmentId, requestedAssignmentId);
    }

    @Override
    public Optional<AssignmentSwitchRequest> findBySlotsAndUsers(long offeredSlotId, String offeringUserId, long requestedSlotId, String requestedUserId) {
        return assignmentSwitchRequestRepository.findBySlotsAndUsers(offeredSlotId, offeringUserId, requestedSlotId, requestedUserId);
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

    @Override
    public Optional<AssignmentSwitchRequest> findInverseTrade(AssignmentSwitchRequest trade) {
        return assignmentSwitchRequestRepository.findByAssignmentIds(
            trade.getRequestedAssignment().getId(),
            trade.getOfferingAssignment().getId()
        );
    }
}
