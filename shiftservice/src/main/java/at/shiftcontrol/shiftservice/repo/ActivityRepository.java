package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {
    @Query("""
        SELECT a FROM Activity a
        WHERE a.location.id = :locationId
        """)
    Collection<Activity> findAllByLocationId(Long locationId);

    @Query("""
        SELECT a FROM Activity a
        WHERE a.event.id = :eventId
        """)
    Collection<Activity> findAllByEventId(Long eventId);


    @Query("""
        SELECT a
        FROM Activity a, ShiftPlan sp
        WHERE a.location IS NULL
        AND sp.id = :shiftPlanId
        AND a.event = sp.event
        """)
    Collection<Activity> findAllWithoutLocationByShiftPlanId(Long shiftPlanId);

    @Query("""
        SELECT a
        FROM Activity a
        WHERE a.event.id = :eventId
        AND a.name = :name
        """)
    Optional<Activity> findByEventAndName(Long eventId, String name);
}
