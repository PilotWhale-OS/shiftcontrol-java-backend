package at.shiftcontrol.shiftservice.dto.rewardpoints;

import at.shiftcontrol.shiftservice.entity.RewardPointTransaction;

public record BookingResultDto(
    boolean created,
    RewardPointTransaction transaction
) {
}
