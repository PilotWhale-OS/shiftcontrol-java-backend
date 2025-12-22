package at.shiftcontrol.shiftservice.repo.userprofile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignmentId;
import at.shiftcontrol.shiftservice.type.NotificationType;

public interface NotificationRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
    List<VolunteerNotificationAssignment> findAllById_VolunteerId(String volunteerId);

    List<VolunteerNotificationAssignment> findAllById_VolunteerIdAndId_NotificationType(
        String volunteerId,
        NotificationType notificationType
    );
}
