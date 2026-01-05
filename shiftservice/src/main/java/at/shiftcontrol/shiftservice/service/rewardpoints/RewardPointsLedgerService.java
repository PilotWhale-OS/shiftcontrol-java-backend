package at.shiftcontrol.shiftservice.service.rewardpoints;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.rewardpoints.BookingResultDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import com.fasterxml.jackson.databind.JsonNode;

public interface RewardPointsLedgerService {
    /* ========= WRITE (append-only) ========= */

    BookingResultDto bookEarn(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        JsonNode metadata
    );

    BookingResultDto bookReversal(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int pointsSnapshot,
        String sourceKey,
        JsonNode metadata
    );

    BookingResultDto bookManualAdjust(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        JsonNode metadata
    );

    /* ========= READ (aggregation) ========= */

    long getTotalPoints(String userId);

    long getPointsForEvent(String userId, long eventId);

    List<EventPointsDto> getPointsGroupedByEvent(String userId);
}
