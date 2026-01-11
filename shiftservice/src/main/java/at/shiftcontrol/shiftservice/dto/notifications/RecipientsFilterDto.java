package at.shiftcontrol.shiftservice.dto.notifications;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.type.ReceiverAccessLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RecipientsFilterDto {

    @NotNull
    @Schema(description = "The type of notifications that potential recipients can toggle to receive")
    NotificationType notificationType;

    @NotNull
    @Schema(description = "The channel which the notification has to be enabled for")
    NotificationChannel notificationChannel;

    @NotNull
    @Schema(description = "The type of access that recipients must have to the specified related plan/event, or event-wide if unspecified")
    ReceiverAccessLevel receiverAccessLevel;

    @Schema(description = "Optional filter to only include recipients related to a specific shift plan. XOR with relatedEventId")
    String relatedShiftPlanId;

    @Schema(description = "Optional filter to only include recipients related to a specific event. XOR with relatedShiftPlanId")
    String relatedEventId;

    @Schema(description = "Optional filter to only include specific volunteers")
    Set<String> relatedVolunteerIds;
}
