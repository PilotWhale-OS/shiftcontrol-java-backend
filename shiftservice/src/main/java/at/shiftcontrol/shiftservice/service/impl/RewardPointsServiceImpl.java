package at.shiftcontrol.shiftservice.service.impl;

import java.util.UUID;

import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsSnapshotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardPointsServiceImpl implements RewardPointsService {

    private final RewardPointsCalculator calculator;
    private final RewardPointsLedgerService ledgerService;


    @Override
    @Transactional
    public void onAssignmentCreated(@NonNull Assignment assignment, @NonNull PositionSlot slot, @NonNull Shift shift) {
        RewardPointsSnapshotDto snapshot =
            calculator.calculateForAssignment(slot, shift); // TODO calculation should not be done here

        // lock snapshot on assignment
        assignment.setAcceptedRewardPoints(snapshot.acceptedRewardPoints());

        String sourceKey = sourceKeyJoin(slot.getId(), assignment.getAssignedVolunteer().getId());

        ledgerService.bookEarn(
            assignment.getAssignedVolunteer().getId(),
            shift.getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            snapshot.acceptedRewardPoints(),
            sourceKey,
            snapshot.metadata()
        );
    }

    // TODO Consider setting currentSlot points when creating/updating slot and when creating/updating shift bonus points and role ppm (or setting roles in general); ALso recalculate for overrideslot point

    private String sourceKeyJoin(long slotId, String volunteerId) {
        return "JOIN:" + slotId + ":" + volunteerId;
    }


    @Override
    @Transactional
    public void onAssignmentRemoved(Assignment assignment, PositionSlot slot, Shift shift) {
        int pointsSnapshot = assignment.getAcceptedRewardPoints();

        String sourceKey = sourceKeyLeave(slot.getId(), assignment.getAssignedVolunteer().getId());

        ledgerService.bookReversal(
            assignment.getAssignedVolunteer().getId(),
            shift.getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            pointsSnapshot,
            sourceKey,
            null
        );
    }

    private String sourceKeyLeave(long slotId, String volunteerId) {
        return "LEAVE:" + slotId + ":" + volunteerId;
    }

    @Override
    @Transactional
    public void onAssignmentReassigned(@NonNull Assignment oldAssignment, @NonNull Assignment newAssignment, @NonNull PositionSlot slot, @NonNull Shift shift) {
        // reverse old assignment
        int oldPointsSnapshot = oldAssignment.getAcceptedRewardPoints();

        String reversalKey = sourceKeyReassignReversal(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        ledgerService.bookReversal(
            oldAssignment.getAssignedVolunteer().getId(),
            shift.getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            oldPointsSnapshot,
            reversalKey,
            null
        );

        // re-calculate + earn for new assignment
        RewardPointsSnapshotDto newSnapshot =
            calculator.calculateForAssignment(slot, shift); // TODO calculation should not be done here

        newAssignment.setAcceptedRewardPoints(newSnapshot.acceptedRewardPoints());

        String earnKey = sourceKeyReassignEarn(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        ledgerService.bookEarn(
            newAssignment.getAssignedVolunteer().getId(),
            shift.getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            newSnapshot.acceptedRewardPoints(),
            earnKey,
            newSnapshot.metadata()
        );
    }

    private String sourceKeyReassignReversal(long slotId, String fromVolunteerId, String toVolunteerId) {
        return "REASSIGN:REV:" + slotId + ":" + fromVolunteerId + ":" + toVolunteerId;
    }

    private String sourceKeyReassignEarn(long slotId, String fromVolunteerId, String toVolunteerId) {
        return "REASSIGN:EARN:" + slotId + ":" + fromVolunteerId + ":" + toVolunteerId;
    }

    @Override
    public void manualAdjust(String volunteerId, long eventId, int points, String reason) {
        if (points == 0) {
            return; // no-op
        }

        String sourceKey = "MANUAL:" + UUID.randomUUID();

        JsonNode metadata = null;
        if (reason != null) {
            metadata = JsonNodeFactory.instance.objectNode()
                .put("reason", reason);
        }

        ledgerService.bookManualAdjust(
            volunteerId,
            eventId,
            null,
            null,
            points,
            sourceKey,
            metadata
        );
    }
}
    

    
