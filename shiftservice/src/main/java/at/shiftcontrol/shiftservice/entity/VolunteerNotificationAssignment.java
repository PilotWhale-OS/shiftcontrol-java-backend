package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        return "VolunteerNotificationAssignment{" +
            "volunteerNotificationAssignmentId=" + volunteerNotificationAssignmentId +
            ", notificationChannel=" + getNotificationChannel() +
            '}';
    }
}
