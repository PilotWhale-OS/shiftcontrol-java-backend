package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignment")
public class Assignment {

    @EmbeddedId
    private AssignmentId id;

    @NotNull
    @MapsId("positionSlotId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "position_slot_id", nullable = false)
    private PositionSlot positionSlot;

    @NotNull
    @MapsId("volunteerId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_volunteer_id", nullable = false)
    private Volunteer assignedVolunteer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @OneToMany(mappedBy = "offeringAssignment")
    private java.util.Collection<AssignmentSwitchRequest> outgoingSwitchRequests;

    @OneToMany(mappedBy = "requestedAssignment")
    private java.util.Collection<AssignmentSwitchRequest> incomingSwitchRequests;

}
