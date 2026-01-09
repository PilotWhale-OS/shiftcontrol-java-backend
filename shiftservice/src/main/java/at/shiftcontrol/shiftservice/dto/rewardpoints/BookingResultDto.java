package at.shiftcontrol.shiftservice.dto.rewardpoints;

import at.shiftcontrol.lib.entity.RewardPointTransaction;

public record BookingResultDto(
    boolean created,
    RewardPointTransaction transaction
) {
}
