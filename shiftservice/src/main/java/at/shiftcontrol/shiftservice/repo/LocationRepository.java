package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l WHERE l.event.id = :eventId")
    Collection<Location> findAllByEventId(Long eventId);
}
