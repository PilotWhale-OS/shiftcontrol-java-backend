package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "volunteer_notification_assignment")
public class VolunteerNotificationAssignment {
    @EmbeddedId
    VolunteerNotificationAssignmentId volunteerNotificationAssignmentId;

    public NotificationType getNotificationType() {
        return this.volunteerNotificationAssignmentId.getNotificationType();
    }

    public NotificationChannel getNotificationChannel() {
        return this.volunteerNotificationAssignmentId.getNotificationChannel();
    }

    @Override
    public String toString() {
        return "VolunteerNotificationAssignment{"
            + "volunteerNotificationAssignmentId=" + volunteerNotificationAssignmentId
            + ", notificationChannel=" + getNotificationChannel()
            + '}';
    }
}
