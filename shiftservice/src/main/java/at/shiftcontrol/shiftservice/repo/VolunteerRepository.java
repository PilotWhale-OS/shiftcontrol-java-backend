package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findById(String userId);

    @Query("""
            select (count(sp) > 0)
            from Volunteer v
            join v.volunteeringPlans sp
            where v.id = :volunteerId and sp.id = :shiftPlanId
        """)
    boolean isVolunteerInShiftPlan(@Param("volunteerId") String volunteerId,
                                   @Param("shiftPlanId") long shiftPlanId);

    @Query("""
            select (count(sp) > 0)
            from Volunteer v
            join v.planningPlans sp
            where v.id = :volunteerId and sp.id = :shiftPlanId
        """)
    boolean isPlannerInShiftPlan(@Param("volunteerId") String volunteerId,
                                 @Param("shiftPlanId") long shiftPlanId);

    @Query("""
            select (count(r) > 0)
            from Volunteer v
            join v.roles r
            where v.id = :volunteerId and r.id = :roleId
        """)
    boolean hasUserRole(@Param("volunteerId") String volunteerId,
                        @Param("roleId") long roleId);

    @Query("""
            SELECT v
            FROM Volunteer v
            JOIN v.planningPlans p
            WHERE p.id = :shiftPlanId
        """)
    Collection<Volunteer> findAllByShiftPlan(long shiftPlanId);
}
