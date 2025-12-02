package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.TradeStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignment_switch_request")
public class AssignmentSwitchRequest {

    @EmbeddedId
    private AssignmentSwitchRequestId id;

    @MapsId("requester")
    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "requester_position_slot_id", referencedColumnName = "position_slot_id"),
            @JoinColumn(name = "requester_volunteer_id", referencedColumnName = "volunteer_id")
    })
    private Assignment requesterAssignment;

    @MapsId("requested")
    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "requested_position_slot_id", referencedColumnName = "position_slot_id"),
            @JoinColumn(name = "requested_volunteer_id", referencedColumnName = "volunteer_id")
    })
    private Assignment requestedAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(nullable = true)
    private String reason;
}
