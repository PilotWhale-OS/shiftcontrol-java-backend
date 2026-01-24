package at.shiftcontrol.auditlog.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogSearchDto {
    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    private String actingUserId;

    /**
     * Substring match for routing key.
     */
    private String routingKey;

    private String eventType;
}
