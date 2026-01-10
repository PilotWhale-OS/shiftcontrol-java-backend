package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.lib.type.TradeStatus;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;

@RequiredArgsConstructor
@Component
public class AssignmentSwitchRequestDaoImpl implements AssignmentSwitchRequestDao {
    private final AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Override
    public String getName() {
        return "AssignmentSwitchRequest";
    }

    @Override
    public Optional<AssignmentSwitchRequest> findById(AssignmentSwitchRequestId id) {
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

    @Override
    public Optional<AssignmentSwitchRequest> findInverseTrade(AssignmentSwitchRequest trade) {
        return assignmentSwitchRequestRepository.findById(
            new AssignmentSwitchRequestId(
                trade.getId().getRequested(),
                trade.getId().getOffering()
            )
        );
    }
}
