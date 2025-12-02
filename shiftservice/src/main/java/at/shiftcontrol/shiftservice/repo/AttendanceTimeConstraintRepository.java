package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceTimeConstraintRepository extends JpaRepository<AttendanceTimeConstraint, Long> {

}