package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.lib.entity.RewardPointTransaction;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;

public interface RewardPointTransactionDao extends BasicDao<RewardPointTransaction, Long> {

    long sumPointsByVolunteer(String volunteerId);

    long sumPointsByVolunteerAndEvent(String volunteerId, long eventId);

    Collection<EventPointsDto> sumPointsGroupedByEvent(String volunteerId);

    Collection<RewardPointTransaction> findAllByVolunteerIdOrderByCreatedAtAsc(String volunteerId);
}
