package at.shiftcontrol.shiftservice.service.impl.rewardpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsSnapshotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;
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
    @IsNotAdmin
    public void onAssignmentCreated(@NonNull Assignment assignment, @NonNull String acceptedRewardPointsHash) throws ConflictException {
        PositionSlot slot = assignment.getPositionSlot();
        validateHash(slot, acceptedRewardPointsHash);

        RewardPointsSnapshotDto snapshot = calculator.calculateForAssignment(slot);

        String sourceKey = sourceKeyJoin(slot.getId(), assignment.getAssignedVolunteer().getId());

        var result = ledgerService.bookEarn(
            assignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            snapshot.rewardPoints(),
            sourceKey,
            snapshot.metadata()
        );

        if (result.created()) {
            // only set if booking was created
            assignment.setAcceptedRewardPoints(result.transaction().getPoints());
        }
    }

    private String sourceKeyJoin(long slotId, String volunteerId) {
        return "JOIN:" + slotId + ":" + volunteerId;
    }


    @Override
    @Transactional
    @IsNotAdmin
    public void onAssignmentRemoved(@NonNull Assignment assignment) {
        PositionSlot slot = assignment.getPositionSlot();
        int pointsSnapshot = assignment.getAcceptedRewardPoints();

        String sourceKey = sourceKeyLeave(slot.getId(), assignment.getAssignedVolunteer().getId());

        var result = ledgerService.bookReversal(
            assignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            pointsSnapshot,
            sourceKey,
            null
        );

        if (result.created()) {
            // only clear if booking was created
            assignment.setAcceptedRewardPoints(0);
        }
    }

    private String sourceKeyLeave(long slotId, String volunteerId) {
        return "LEAVE:" + slotId + ":" + volunteerId;
    }

    @Override
    @Transactional
    @IsNotAdmin
    public void onAssignmentReassigned(@NonNull Assignment oldAssignment, @NonNull Assignment newAssignment,
                                       @NonNull String acceptedRewardPointsHash) throws ConflictException {
        PositionSlot slot = oldAssignment.getPositionSlot();
        // reverse old assignment
        int oldPointsSnapshot = oldAssignment.getAcceptedRewardPoints();

        String reversalKey = sourceKeyReassignReversal(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        var unassignmentResult = ledgerService.bookReversal(
            oldAssignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            oldPointsSnapshot,
            reversalKey,
            null
        );

        if (unassignmentResult.created()) {
            // only clear if booking was created
            oldAssignment.setAcceptedRewardPoints(0);
        }

        validateHash(slot, acceptedRewardPointsHash);

        // re-calculate + earn for new assignment
        RewardPointsSnapshotDto newSnapshot = calculator.calculateForAssignment(slot);

        String earnKey = sourceKeyReassignEarn(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        var assignmentResult = ledgerService.bookEarn(
            newAssignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            newSnapshot.rewardPoints(),
            earnKey,
            newSnapshot.metadata()
        );

        if (assignmentResult.created()) {
            // only set if booking for new assigment was created
            newAssignment.setAcceptedRewardPoints(newSnapshot.rewardPoints());
        }
    }

    private void validateHash(@NonNull PositionSlot slot, @NonNull String acceptedRewardPointsHash) throws ConflictException {
        String currentHash = calculator.calculatePointsConfigHash(slot);

        if (!currentHash.equals(acceptedRewardPointsHash)) {
            throw new ConflictException("Reward points configuration has changed since volunteer accepted assignment");
        }
    }

    private String sourceKeyReassignReversal(long slotId, String fromVolunteerId, String toVolunteerId) {
        return "REASSIGN:REV:" + slotId + ":" + fromVolunteerId + ":" + toVolunteerId;
    }

    private String sourceKeyReassignEarn(long slotId, String fromVolunteerId, String toVolunteerId) {
        return "REASSIGN:EARN:" + slotId + ":" + fromVolunteerId + ":" + toVolunteerId;
    }

    @Override
    @AdminOnly
    public void manualAdjust(String volunteerId, long eventId, int points, String reason) {
        if (points == 0) {
            return; // no-op
        }

        String sourceKey = "MANUAL:" + UUID.randomUUID();

        Map<String, Object> metadata = new HashMap<>();
        if (reason != null) {
            metadata.put("reason", reason);
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
    

    
