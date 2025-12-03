package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

@Repository
public interface AttendanceTimeConstraintRepository extends JpaRepository<AttendanceTimeConstraint, Long> {
}
