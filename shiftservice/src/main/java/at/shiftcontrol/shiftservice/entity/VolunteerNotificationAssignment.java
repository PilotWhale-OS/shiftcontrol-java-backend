package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @Id
    private long volunteerId;
    @NotNull
    private NotificationType notificationType;
    @NotNull
    private NotificationChannel notificationChannel;
}
