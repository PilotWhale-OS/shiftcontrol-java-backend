package at.shiftcontrol.shiftservice.dto.rewardpoints;

import at.shiftcontrol.shiftservice.entity.RewardPointsTransaction;

public record BookingResultDto(
    boolean created,
    RewardPointsTransaction transaction
) {
}
