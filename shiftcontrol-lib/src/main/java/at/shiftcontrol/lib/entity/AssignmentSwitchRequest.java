package at.shiftcontrol.lib.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import at.shiftcontrol.lib.type.TradeStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignment_switch_request")
public class AssignmentSwitchRequest {
    @EmbeddedId
    private AssignmentSwitchRequestId id;
    @NotNull
    @MapsId("offering")
    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "offering_position_slot_id", referencedColumnName = "position_slot_id"),
        @JoinColumn(name = "offering_volunteer_id", referencedColumnName = "assigned_volunteer_id")
    })
    private Assignment offeringAssignment;
    @NotNull
    @MapsId("requested")
    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "requested_position_slot_id", referencedColumnName = "position_slot_id"),
        @JoinColumn(name = "requested_volunteer_id", referencedColumnName = "assigned_volunteer_id")
    })
    private Assignment requestedAssignment;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;
    @NotNull
    @Column(nullable = false)
    private Instant createdAt;

    @Override
    public String toString() {
        return "AssignmentSwitchRequest{"
            + "id=" + id
            + ", status=" + status
            + ", createdAt=" + createdAt
            + '}';
    }
}
