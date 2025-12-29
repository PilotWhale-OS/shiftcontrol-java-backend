package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @Query("""
        SELECT a FROM Activity a
        WHERE a.location.id = :locationId
        """)
    Collection<Activity> findAllByLocationId(Long locationId);
}
