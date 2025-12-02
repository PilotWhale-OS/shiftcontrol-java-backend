package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "position_slot")
public class PositionSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // private Collection<Volunteer> assignedVolunteers; // TODO paul needed ? Assignment exists

    //@NonNull
    //private Location location; // TODO flo patrick needed or stays in shift?

    @Column(nullable = false)
    private int desiredVolunteerCount;
}
