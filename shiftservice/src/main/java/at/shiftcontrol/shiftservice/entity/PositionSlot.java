package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    @OneToMany(mappedBy = "positionSlot")
    private Collection<Assignment> assignments;

    @NotNull
    @Column(nullable = false)
    private int desiredVolunteerCount;
}
