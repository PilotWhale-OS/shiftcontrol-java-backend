package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import jakarta.persistence.*;
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

    // positionSlotId from PK → FK
    @MapsId("positionSlotId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "position_slot_id", nullable = false)
    private PositionSlot positionSlot;

    // volunteerId from PK → FK
    @MapsId("volunteerId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_volunteer_id", nullable = false)
    private Volunteer assignedVolunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private AssignmentStatus status;

    @OneToMany(mappedBy = "offeringAssignment")
    private java.util.Collection<AssignmentSwitchRequest> outgoingSwitchRequests;

    @OneToMany(mappedBy = "requestedAssignment")
    private java.util.Collection<AssignmentSwitchRequest> incomingSwitchRequests;

}
