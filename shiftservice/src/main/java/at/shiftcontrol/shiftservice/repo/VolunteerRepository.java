package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Volunteer;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findById(String userId);

    @Query("""
            SELECT v
            FROM Volunteer v
            JOIN v.planningPlans p
            WHERE p.id = :shiftPlanId
        """)
    Collection<Volunteer> findAllByShiftPlan(long shiftPlanId);
}
