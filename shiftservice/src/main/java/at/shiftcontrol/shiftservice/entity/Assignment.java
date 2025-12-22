package at.shiftcontrol.shiftservice.entity;

import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;

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
    private Collection<AssignmentSwitchRequest> outgoingSwitchRequests;
    @OneToMany(mappedBy = "requestedAssignment")
    private Collection<AssignmentSwitchRequest> incomingSwitchRequests;
}
