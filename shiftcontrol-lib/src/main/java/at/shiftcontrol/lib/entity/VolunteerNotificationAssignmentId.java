package at.shiftcontrol.lib.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class VolunteerNotificationAssignmentId implements Serializable {
    @Column(nullable = false)
    private String volunteerId;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel notificationChannel;

    public static VolunteerNotificationAssignmentId of(
        String volunteerId,
        NotificationType notificationType,
        NotificationChannel notificationChannel
    ) {
        return VolunteerNotificationAssignmentId.builder()
            .volunteerId(volunteerId)
            .notificationType(notificationType)
            .notificationChannel(notificationChannel)
            .build();
    }
}
