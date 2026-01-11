package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import at.shiftcontrol.lib.entity.TrustAlert;

public interface TrustAlertRepository extends JpaRepository<TrustAlert, Long> {

    @Query("""
        SELECT a FROM TrustAlert a
        WHERE a.positionSlot.shift.shiftPlan.event.id = :eventId
        """)
    Collection<TrustAlert> findAllByEventId(long eventId);
}
