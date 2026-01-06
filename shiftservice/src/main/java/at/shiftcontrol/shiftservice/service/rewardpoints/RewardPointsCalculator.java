package at.shiftcontrol.shiftservice.service.rewardpoints;

import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsSnapshotDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;

public interface RewardPointsCalculator {
    RewardPointsSnapshotDto calculateForAssignment(PositionSlot slot);

    String calculatePointsConfigHash(PositionSlot slot);
}
