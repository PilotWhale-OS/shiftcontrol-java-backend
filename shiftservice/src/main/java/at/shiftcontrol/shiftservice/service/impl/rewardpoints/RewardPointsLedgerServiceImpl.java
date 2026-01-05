package at.shiftcontrol.shiftservice.service.impl.rewardpoints;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.rewardpoints.BookingResultDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.TotalPointsDto;
import at.shiftcontrol.shiftservice.entity.RewardPointTransaction;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.type.RewardPointTransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardPointsLedgerServiceImpl implements RewardPointsLedgerService {
    private final RewardPointTransactionDao dao;
    private final VolunteerDao volunteerDao;
    private final EventDao eventDao;

    @Override
    @Transactional
    public BookingResultDto bookEarn(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        JsonNode metadata
    ) {
        return insertIdempotent(
            userId,
            eventId,
            shiftPlanId,
            positionSlotId,
            points,
            RewardPointTransactionType.EARN,
            sourceKey,
            metadata
        );
    }

    @Override
    @Transactional
    public BookingResultDto bookReversal(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int pointsSnapshot,
        String sourceKey,
        JsonNode metadata
    ) {
        // reversal is always negative of the snapshot
        int points = -pointsSnapshot;

        return insertIdempotent(
            userId,
            eventId,
            shiftPlanId,
            positionSlotId,
            points,
            RewardPointTransactionType.REVERSAL,
            sourceKey,
            metadata
        );
    }

    @Override
    @Transactional
    public BookingResultDto bookManualAdjust(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        String sourceKey,
        JsonNode metadata
    ) {
        return insertIdempotent(
            userId,
            eventId,
            shiftPlanId,
            positionSlotId,
            points,
            RewardPointTransactionType.MANUAL_ADJUST,
            sourceKey,
            metadata
        );
    }

    private BookingResultDto insertIdempotent(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        RewardPointTransactionType type,
        String sourceKey,
        JsonNode metadata
    ) {
        validateInputs(userId, eventId, points, sourceKey, type);

        RewardPointTransaction tx = RewardPointTransaction.builder()
            .volunteerId(userId)
            .eventId(eventId)
            .shiftPlanId(shiftPlanId) // optional
            .positionSlotId(positionSlotId) // optional
            .points(points)
            .type(type)
            .sourceKey(sourceKey)
            .metadata(metadata)
            .createdAt(Instant.now())
            .build();

        try {
            RewardPointTransaction saved = dao.save(tx);
            return new BookingResultDto(true, saved);
        } catch (DataIntegrityViolationException e) {
            // Most likely UNIQUE(source_key) violated -> idempotent "already booked"
            // Return created=false. If you want, you can query by sourceKey and return that entity.
            return new BookingResultDto(false, null);
        }
    }

    private void validateInputs(String userId, long eventId, int points, String sourceKey, RewardPointTransactionType type) {
        volunteerDao.findById(userId).orElseThrow(() ->
            new IllegalArgumentException("userId does not exist: " + userId)
        );
        eventDao.findById(eventId).orElseThrow(() ->
            new IllegalArgumentException("eventId does not exist: " + eventId)
        );
        if (sourceKey == null || sourceKey.isBlank()) {
            throw new IllegalArgumentException("sourceKey must not be blank");
        }
        // allow negative points only for REVERSAL and MANUAL_ADJUST
        if (type == RewardPointTransactionType.EARN && points < 0) {
            throw new IllegalArgumentException("EARN points must be >= 0");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IsNotAdmin
    public TotalPointsDto getTotalPoints(String userId) {
        var totalPoints = dao.sumPointsByVolunteer(userId);

        return TotalPointsDto.builder()
            .totalPoints((int) totalPoints)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    @IsNotAdmin
    public EventPointsDto getPointsForEvent(String userId, long eventId) {
        var evenPoints = dao.sumPointsByVolunteerAndEvent(userId, eventId);

        return EventPointsDto.builder()
            .eventId(String.valueOf(eventId))
            .points((int) evenPoints)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    @IsNotAdmin
    public Collection<EventPointsDto> getPointsGroupedByEvent(String userId) {
        return dao.sumPointsGroupedByEvent(userId);
    }
}
