package at.shiftcontrol.auditlog.entity;

import java.time.Instant;
import java.util.StringJoiner;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Immutable
@Entity
public class LogEntry {
    @Id
    private UUID id;

    private String routingKey;

    private String actingUserId;
    private String traceId;
    @NotNull
    private Instant timestamp;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Override
    public String toString() {
        return new StringJoiner(", ", LogEntry.class.getSimpleName() + "[", "]")
            .add("timestamp=" + timestamp)
            .add("traceId='" + traceId + "'")
            .add("actingUserId='" + actingUserId + "'")
            .add("routingKey='" + routingKey + "'")
            .add("id=" + id)
            .toString();
    }
}
