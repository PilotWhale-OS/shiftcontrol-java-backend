package at.shiftcontrol.shiftservice.dto.rewardpoints;

import at.shiftcontrol.lib.entity.RewardPointsTransaction;

public record BookingResultDto(
    boolean created,
    RewardPointsTransaction transaction
) {
}
