package at.shiftcontrol.shiftservice.service.impl.rewardpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsSnapshotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.mapper.RewardPointsMapper;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;

@Service
@RequiredArgsConstructor
public class RewardPointsServiceImpl implements RewardPointsService {
    private final RewardPointsCalculator calculator;
    private final RewardPointsLedgerService ledgerService;

    @Override
    @Transactional
    @IsNotAdmin
    public void onAssignmentAccepted(Assignment assignment) throws ConflictException {
        // no need to validate hash, because not available when planner accepts signup request
        onAssignmentJoined(assignment);
    }

    @Override
    @Transactional
    @IsNotAdmin
    public void onAssignmentCreated(@NonNull Assignment assignment, @NonNull String acceptedRewardPointsHash) throws ConflictException {
        PositionSlot slot = assignment.getPositionSlot();
        validateHash(slot, acceptedRewardPointsHash);

        onAssignmentJoined(assignment);
    }

    private void onAssignmentJoined(@NonNull Assignment assignment) {
        RewardPointsSnapshotDto snapshot = calculator.calculateForAssignment(assignment.getPositionSlot());

        String sourceKey = sourceKeyJoin(assignment.getPositionSlot().getId(), assignment.getAssignedVolunteer().getId());

        var result = ledgerService.bookEarn(RewardPointsMapper.toRewardPointsTransactionDto(
            assignment,
            snapshot.rewardPoints(),
            sourceKey,
            snapshot.metadata()
        ));

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

        var result = ledgerService.bookReversal(RewardPointsMapper.toRewardPointsTransactionDto(
            assignment,
            pointsSnapshot,
            sourceKey,
            null
        ));

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
    public void onAssignmentReassignedAuction(@NonNull Assignment oldAssignment, @NonNull Assignment newAssignment,
                                              @NonNull String acceptedRewardPointsHash) throws ConflictException {
        // recalculate points for new assignment via auction
        onAssignmentReassigned(oldAssignment, newAssignment, acceptedRewardPointsHash, true);
    }

    @Override
    @Transactional
    @IsNotAdmin
    public void onAssignmentReassignedTrade(@NonNull Assignment oldAssignment, @NonNull Assignment newAssignment) throws ConflictException {
        // simply reuse old points snapshot and do not recalculate
        onAssignmentReassigned(oldAssignment, newAssignment, null, false);
    }

    private void onAssignmentReassigned(@NonNull Assignment oldAssignment, @NonNull Assignment newAssignment,
                                        String acceptedRewardPointsHash, boolean recalculate) throws ConflictException {
        PositionSlot slot = oldAssignment.getPositionSlot();
        // reverse old assignment
        int oldPointsSnapshot = oldAssignment.getAcceptedRewardPoints();

        String reversalKey = sourceKeyReassignReversal(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        var unassignmentResult = ledgerService.bookReversal(RewardPointsMapper.toRewardPointsTransactionDto(
            oldAssignment,
            oldPointsSnapshot,
            reversalKey,
            null
        ));

        if (unassignmentResult.created()) {
            // only clear if booking was created
            oldAssignment.setAcceptedRewardPoints(0);
        }


        RewardPointsSnapshotDto newSnapshot;
        if (recalculate) {
            if (acceptedRewardPointsHash == null) {
                throw new IllegalArgumentException("acceptedRewardPointsHash must not be null when recalculating");
            }
            // re-calculate + earn for new assignment
            validateHash(slot, acceptedRewardPointsHash);
            newSnapshot = calculator.calculateForAssignment(slot);
        } else {
            if (acceptedRewardPointsHash != null) {
                throw new IllegalArgumentException("acceptedRewardPointsHash must be null when not recalculating");
            }

            // use old snapshot
            newSnapshot = new RewardPointsSnapshotDto(
                oldPointsSnapshot,
                Map.of("note", "reused points from previous assignment because of trade")
            );
        }

        String earnKey = sourceKeyReassignEarn(
            slot.getId(),
            oldAssignment.getAssignedVolunteer().getId(),
            newAssignment.getAssignedVolunteer().getId()
        );

        var assignmentResult = ledgerService.bookEarn(RewardPointsMapper.toRewardPointsTransactionDto(
            newAssignment,
            newSnapshot.rewardPoints(),
            earnKey,
            newSnapshot.metadata()
        ));

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

        ledgerService.bookManualAdjust(RewardPointsMapper.toRewardPointsTransactionDto(
            volunteerId,
            eventId,
            null,
            null,
            points,
            sourceKey,
            metadata
        ));
    }
}



