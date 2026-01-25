package at.shiftcontrol.auditlog.service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import at.shiftcontrol.auditlog.dto.LogEntryCreateDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventBusListenerService {
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF
        = new TypeReference<>() {};

    @RabbitListener(queues = "${audit.rabbitmq.queue}")
    public void onMessage(
        @Payload byte[] body,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) throws IOException {
        var root = objectMapper.readTree(body); // JsonNode

        String actingUserId  = root.path("actingUserId").isMissingNode() ? null : root.path("actingUserId").asText(null);
        String traceId       = root.path("traceId").isMissingNode() ? null : root.path("traceId").asText(null);
        String eventType     = root.path("eventType").isMissingNode() ? null :  root.path("eventType").asText(null);
        String description   = root.path("description").isMissingNode() ? null :  root.path("description").asText(null);

        var timestampNode   = root.get("timestamp"); // keep as JsonNode if you want
        var instant = getTimestamp(timestampNode);
        // remove metadata fields from payload
        if (root.isObject()) {
            var obj = (ObjectNode) root;
            obj.remove("actingUserId");
            obj.remove("traceId");
            obj.remove("timestamp");
            obj.remove("eventType");
            obj.remove("description");
        }

        ObjectNode payload = null;
        if (root instanceof ObjectNode objectnode) {
            payload = objectnode;
        }

        var createDto = LogEntryCreateDto.builder()
            .routingKey(routingKey)
            .eventType(eventType)
            .description(description)
            .actingUserId(actingUserId)
            .traceId(traceId)
            .timestamp(instant)
            .payload(payload)
            .build();

        var entry = auditLogService.create(createDto);
        log.info("Created audit log entry: {}", entry);
    }

    private static Instant getTimestamp(JsonNode timestampNode) {
        Instant instant;
        if (timestampNode.isNumber()) {
            double ts = timestampNode.asDouble();
            long sec = (long) ts;
            long nanos = (long) ((ts - sec) * 1_000_000_000);
            instant = Instant.ofEpochSecond(sec, nanos);
        } else if (timestampNode.isTextual()) {
            instant = Instant.parse(timestampNode.asText());
        } else {
            instant = Instant.now();
        }
        return instant;
    }
}
