package at.shiftcontrol.auditlog.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.node.ObjectNode;

@Data
@Builder
public class LogEntryCreateDto {
    private String routingKey;
    private String eventType;
    private String description;
    private String actingUserId;
    private String traceId;
    private Instant timestamp;

    private ObjectNode payload;
}
