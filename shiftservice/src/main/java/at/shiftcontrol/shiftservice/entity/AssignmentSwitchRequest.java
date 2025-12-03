package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.TradeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
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
}
