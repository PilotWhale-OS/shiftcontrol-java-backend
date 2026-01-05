package at.shiftcontrol.shiftservice.service.rewardpoints;

import java.util.Collection;
import java.util.Map;

import at.shiftcontrol.shiftservice.dto.rewardpoints.BookingResultDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.TotalPointsDto;

public interface RewardPointsLedgerService {
    /* ========= WRITE (append-only) ========= */

    BookingResultDto bookEarn(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        Map<String, Object> metadata
    );

    BookingResultDto bookReversal(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int pointsSnapshot,
        String sourceKey,
        Map<String, Object> metadata
    );

    BookingResultDto bookManualAdjust(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        Map<String, Object> metadata
    );

    /* ========= READ (aggregation) ========= */

    TotalPointsDto getTotalPoints(String userId);

    EventPointsDto getPointsForEvent(String userId, long eventId);

    Collection<EventPointsDto> getPointsGroupedByEvent(String userId);
}
