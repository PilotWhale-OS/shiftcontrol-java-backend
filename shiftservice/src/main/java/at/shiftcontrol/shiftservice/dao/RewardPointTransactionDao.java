package at.shiftcontrol.shiftservice.dao;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.entity.RewardPointTransaction;

public interface RewardPointTransactionDao extends BasicDao<RewardPointTransaction, Long> {

    long sumPointsByVolunteer(long volunteerId);

    long sumPointsByVolunteerAndEvent(long volunteerId, long eventId);

    List<EventPointsDto> sumPointsGroupedByEvent(long volunteerId);
}