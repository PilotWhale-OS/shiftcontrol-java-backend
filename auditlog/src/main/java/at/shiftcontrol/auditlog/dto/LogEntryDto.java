package at.shiftcontrol.auditlog.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.auditlog.entity.LogEntry;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogEntryDto {
    @NotNull
    private String id;

    private String routingKey;

    private String actingUserId;
    private String traceId;
    @NotNull
    private Instant timestamp;

    @JsonRawValue
    private String payload;

    public static LogEntryDto of(LogEntry entity) {
        LogEntryDto dto = new LogEntryDto();
        dto.setId(entity.getId().toString());
        dto.setRoutingKey(entity.getRoutingKey());
        dto.setActingUserId(entity.getActingUserId());
        dto.setTraceId(entity.getTraceId());
        dto.setTimestamp(entity.getTimestamp());
        dto.setPayload(entity.getPayload());
        return dto;
    }

    public static List<LogEntryDto> of(List<LogEntry> entities) {
        return entities.stream()
            .map(LogEntryDto::of)
            .toList();
    }
}
