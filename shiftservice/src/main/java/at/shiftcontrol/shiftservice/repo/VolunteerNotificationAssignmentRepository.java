package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignmentId;

@Repository
public interface VolunteerNotificationAssignmentRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
}
