package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

@Repository
public interface TimeConstraintRepository extends JpaRepository<TimeConstraint, Long> {
    @Query("""
        SELECT tc
        FROM TimeConstraint tc
        WHERE tc.volunteer.id = :volunteerId
          AND tc.event.id = :eventId
        """)
    Collection<TimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId);

    @Query("""
        SELECT tc
        FROM TimeConstraint tc
        WHERE tc.volunteer.id = :volunteerId
          AND tc.event.id = :eventId
          AND tc.type = :type
        """)
    Collection<TimeConstraint> searchByVolunteerAndEventAndType(String volunteerId, long eventId, TimeConstraintType type);
}
