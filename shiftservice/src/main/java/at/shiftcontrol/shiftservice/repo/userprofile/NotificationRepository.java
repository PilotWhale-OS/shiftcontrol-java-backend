package at.shiftcontrol.shiftservice.repo.userprofile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.lib.entity.VolunteerNotificationAssignmentId;
import at.shiftcontrol.lib.type.NotificationType;

public interface NotificationRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerId(String volunteerId);

    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerIdAndVolunteerNotificationAssignmentId_NotificationType(
        String volunteerId,
        NotificationType notificationType
    );
}
