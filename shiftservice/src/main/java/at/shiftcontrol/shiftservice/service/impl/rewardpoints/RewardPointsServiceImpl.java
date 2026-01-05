package at.shiftcontrol.shiftservice.service.impl.rewardpoints;

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
    @IsNotAdmin
    public void onAssignmentCreated(@NonNull Assignment assignment, @NonNull String acceptedRewardPointsHash) throws ConflictException {
        PositionSlot slot = assignment.getPositionSlot();
        validateHash(slot, acceptedRewardPointsHash);

        RewardPointsSnapshotDto snapshot = calculator.calculateForAssignment(slot);


        // lock snapshot on assignment
        assignment.setAcceptedRewardPoints(snapshot.rewardPoints());

        String sourceKey = sourceKeyJoin(slot.getId(), assignment.getAssignedVolunteer().getId());

        ledgerService.bookEarn(
            assignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            snapshot.rewardPoints(),
            sourceKey,
            snapshot.metadata()
        );
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

        ledgerService.bookReversal(
            assignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
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

        ledgerService.bookReversal(
            oldAssignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            oldPointsSnapshot,
            reversalKey,
            null
        );

        validateHash(slot, acceptedRewardPointsHash);

        // re-calculate + earn for new assignment
        RewardPointsSnapshotDto newSnapshot = calculator.calculateForAssignment(slot);

        newAssignment.setAcceptedRewardPoints(newSnapshot.rewardPoints());

        String earnKey = sourceKeyReassignEarn(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        ledgerService.bookEarn(
            newAssignment.getAssignedVolunteer().getId(),
            slot.getShift().getShiftPlan().getEvent().getId(),
            slot.getShift().getShiftPlan().getId(),
            slot.getId(),
            newSnapshot.rewardPoints(),
            earnKey,
            newSnapshot.metadata()
        );
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
    

    
