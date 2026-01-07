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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@Getter
@Setter
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

    @Column(nullable = false)
    private int acceptedRewardPoints;

    @Override
    public String toString() {
        return "Assignment{id=%s, status=%s, outgoingSwitchRequests=%s, incomingSwitchRequests=%s}"
            .formatted(id, status, outgoingSwitchRequests, incomingSwitchRequests);
    }

    public static Assignment of(PositionSlot positionSlot, Volunteer volunteer, AssignmentStatus status) {
        return Assignment.builder()
            .id(AssignmentId.of(positionSlot.getId(), volunteer.getId()))
            .assignedVolunteer(volunteer)
            .positionSlot(positionSlot)
            .status(status)
            .build();
    }
}
