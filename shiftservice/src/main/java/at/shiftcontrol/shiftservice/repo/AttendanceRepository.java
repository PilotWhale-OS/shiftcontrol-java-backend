package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceId;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {
}
