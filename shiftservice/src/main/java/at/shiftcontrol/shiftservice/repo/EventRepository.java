package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.shiftservice.dto.rows.PlanVolunteerIdRow;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    boolean existsByNameIgnoreCase(String name);

    @Query("""
        SELECT new at.shiftcontrol.shiftservice.dto.rows.PlanVolunteerIdRow(
                p.id,
                p.name,
                v.id
            )
            FROM Volunteer v
            JOIN v.planningPlans p
            WHERE p.event.id = :eventId
              AND p IN (
                  SELECT vp
                  FROM Volunteer u
                  JOIN u.volunteeringPlans vp
                  WHERE u.id = :userId
              )
        """)
    Collection<PlanVolunteerIdRow> getPlannersForEventAndUser(long eventId, String userId);
}
