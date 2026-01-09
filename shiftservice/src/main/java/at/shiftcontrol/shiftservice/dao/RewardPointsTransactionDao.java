package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.entity.RewardPointsTransaction;

public interface RewardPointsTransactionDao extends BasicDao<RewardPointsTransaction, Long> {

    long sumPointsByVolunteer(String volunteerId);

    long sumPointsByVolunteerAndEvent(String volunteerId, long eventId);

    Collection<EventPointsDto> sumPointsGroupedByEvent(String volunteerId);

    Collection<RewardPointsTransaction> findAllByVolunteerIdOrderByCreatedAtAsc(String volunteerId);
}