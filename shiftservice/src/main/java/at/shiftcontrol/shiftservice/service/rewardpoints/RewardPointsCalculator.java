package at.shiftcontrol.shiftservice.service.rewardpoints;

import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsSnapshotDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;

public interface RewardPointsCalculator {
    RewardPointsSnapshotDto calculateForAssignment(PositionSlot slot, Shift shift);

    String calculatePointsConfigHash(PositionSlot slot, Shift shift);
}
