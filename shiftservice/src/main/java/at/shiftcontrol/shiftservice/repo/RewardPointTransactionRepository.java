package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsInternalDto;
import at.shiftcontrol.shiftservice.entity.RewardPointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RewardPointTransactionRepository extends JpaRepository<RewardPointTransaction, Long> {
    /**
     * Global points for a volunteer: SUM(points).
     */
    @Query("""
            select coalesce(sum(t.points), 0)
            from RewardPointTransaction t
            where t.volunteerId = :volunteerId
        """)
    long sumPointsByVolunteer(String volunteerId);

    /**
     * Points for a volunteer within one event: SUM(points) filtered by event.
     */
    @Query("""
            select coalesce(sum(t.points), 0)
            from RewardPointTransaction t
            where t.volunteerId = :volunteerId
              and t.eventId = :eventId
        """)
    long sumPointsByVolunteerAndEvent(String volunteerId, Long eventId);

    /**
     * Points per event for a volunteer: GROUP BY event.
     */
    @Query("""
            select new at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsInternalDto(
                t.eventId, coalesce(sum(t.points), 0)
            )
            from RewardPointTransaction t
            where t.volunteerId = :volunteerId
            group by t.eventId
            order by t.eventId
        """)
    List<EventPointsInternalDto> sumPointsGroupedByEvent(String volunteerId);

    List<RewardPointTransaction> findAllByVolunteerIdOrderByCreatedAtAsc(String volunteerId);
}
