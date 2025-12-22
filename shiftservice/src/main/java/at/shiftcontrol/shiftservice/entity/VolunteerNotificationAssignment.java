package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "volunteer_notification_assignment")
public class VolunteerNotificationAssignment {
    @EmbeddedId
    VolunteerNotificationAssignmentId id;

    public NotificationType getNotificationType() {
        return id.getNotificationType();
    }

    public NotificationChannel getNotificationChannel() {
        return id.getNotificationChannel();
    }
}
