package at.shiftcontrol.shiftservice.entity;

import java.time.Instant;

import at.shiftcontrol.shiftservice.type.RewardPointTransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Immutable
@Table(name = "reward_point_transaction",
    indexes = {
        @Index(name = "idx_rpt_volunteer", columnList = "volunteer_id"),
        @Index(name = "idx_rpt_volunteer_event", columnList = "volunteer_id,event_id"),
        @Index(name = "idx_rpt_volunteer_shiftplan", columnList = "volunteer_id,shift_plan_id"),
        @Index(name = "idx_rpt_slot", columnList = "position_slot_id")
    },
    uniqueConstraints = @UniqueConstraint(name = "ux_rpt_source_key", columnNames = "source_key")
)
public class RewardPointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "volunteer_id", nullable = false)
    private String volunteerId;

    @Column(name = "event_id", nullable = false)
    private long eventId;

    @Column(name = "shift_plan_id")
    private Long shiftPlanId;

    @Column(name = "position_slot_id")
    private Long positionSlotId;

    @Column(nullable = false)
    private int points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardPointTransactionType type;

    @Column(name = "source_key", nullable = false, length = 255)
    private String sourceKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;
}
