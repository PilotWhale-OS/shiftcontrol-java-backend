package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class VolunteerNotificationAssignmentId {
    @Column(nullable = false)
    private String volunteerId;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    public static VolunteerNotificationAssignmentId of(String volunteerId, NotificationType notificationType) {
        return VolunteerNotificationAssignmentId.builder()
            .volunteerId(volunteerId)
            .notificationType(notificationType)
            .build();
    }
}
