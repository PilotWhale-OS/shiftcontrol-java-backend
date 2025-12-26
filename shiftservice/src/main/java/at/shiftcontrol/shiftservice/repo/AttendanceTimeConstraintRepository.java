package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

@Repository
public interface AttendanceTimeConstraintRepository extends JpaRepository<AttendanceTimeConstraint, Long> {
    @Query("""
        SELECT atc
        FROM AttendanceTimeConstraint atc
        WHERE atc.attendance.volunteer.id = :volunteerId
          AND atc.attendance.event.id = :eventId
        """)
    Collection<AttendanceTimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId);
}
