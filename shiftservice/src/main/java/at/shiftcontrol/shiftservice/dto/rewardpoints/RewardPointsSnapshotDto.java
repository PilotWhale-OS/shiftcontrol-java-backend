package at.shiftcontrol.shiftservice.dto.rewardpoints;

import com.fasterxml.jackson.databind.JsonNode;

public record RewardPointsSnapshotDto(
    int acceptedRewardPoints,
    JsonNode metadata
) {
}