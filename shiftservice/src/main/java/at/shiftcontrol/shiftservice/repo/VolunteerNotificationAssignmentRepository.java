package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.lib.entity.VolunteerNotificationAssignmentId;

@Repository
public interface VolunteerNotificationAssignmentRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
}
