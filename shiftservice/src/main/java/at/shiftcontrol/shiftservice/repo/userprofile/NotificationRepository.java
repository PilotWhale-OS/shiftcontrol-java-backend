package at.shiftcontrol.shiftservice.repo.userprofile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignmentId;
import at.shiftcontrol.shiftservice.type.NotificationType;

public interface NotificationRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerId(String volunteerId);

    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerIdAndVolunteerNotificationAssignmentId_NotificationType(
        String volunteerId,
        NotificationType notificationType
    );
}
