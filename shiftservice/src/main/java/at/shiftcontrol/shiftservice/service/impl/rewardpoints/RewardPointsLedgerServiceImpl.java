package at.shiftcontrol.shiftservice.service.impl.rewardpoints;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.RewardPointTransaction;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.RewardPointTransactionEvent;
import at.shiftcontrol.lib.type.RewardPointTransactionType;
import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.rewardpoints.BookingResultDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsTransactionDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.TotalPointsDto;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;

@Service
@RequiredArgsConstructor
public class RewardPointsLedgerServiceImpl implements RewardPointsLedgerService {
    private final RewardPointTransactionDao dao;
    private final VolunteerDao volunteerDao;
    private final EventDao eventDao;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public BookingResultDto bookEarn(RewardPointsTransactionDto dto) {
        return insert(
            dto.getUserId(),
            dto.getEventId(),
            dto.getShiftPlanId(),
            dto.getPositionSlotId(),
            dto.getPointsSnapshot(),
            RewardPointTransactionType.EARN,
            dto.getSourceKey(),
            dto.getMetadata()
        );
    }

    @Override
    @Transactional
    public BookingResultDto bookReversal(RewardPointsTransactionDto dto) {
        // reversal is always negative of the snapshot
        int points = -dto.getPointsSnapshot();

        return insert(
            dto.getUserId(),
            dto.getEventId(),
            dto.getShiftPlanId(),
            dto.getPositionSlotId(),
            points,
            RewardPointTransactionType.REVERSAL,
            dto.getSourceKey(),
            dto.getMetadata()
        );
    }

    @Override
    @Transactional
    public BookingResultDto bookManualAdjust(RewardPointsTransactionDto dto) {
        return insert(
            dto.getUserId(),
            dto.getEventId(),
            dto.getShiftPlanId(),
            dto.getPositionSlotId(),
            dto.getPointsSnapshot(),
            RewardPointTransactionType.MANUAL_ADJUST,
            dto.getSourceKey(),
            dto.getMetadata()
        );
    }

    private BookingResultDto insert(
        String userId,
        long eventId,
        Long shiftPlanId,
        Long positionSlotId,
        int points,
        RewardPointTransactionType type,
        String sourceKey,
        Map<String, Object> metadata
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

            publisher.publishEvent(RewardPointTransactionEvent.of(RoutingKeys.format(RoutingKeys.REWARDPOINT_TRANSACTION_CREATED, Map.of(
                "volunteerId", saved.getVolunteerId(),
                "transactionId", saved.getId())), saved
            ));
            return new BookingResultDto(true, saved);
        } catch (DataIntegrityViolationException e) {
            // publish failed event with uncommitted transaction data
            publisher.publishEvent(RewardPointTransactionEvent.of(RoutingKeys.format(RoutingKeys.REWARDPOINT_TRANSACTION_FAILED, Map.of(
                "volunteerId", tx.getVolunteerId())), tx
            ));

            return new BookingResultDto(false, null);
        }
    }

    private void validateInputs(String userId, long eventId, int points, String sourceKey, RewardPointTransactionType type) {
        volunteerDao.getById(userId);
        eventDao.getById(eventId);
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
