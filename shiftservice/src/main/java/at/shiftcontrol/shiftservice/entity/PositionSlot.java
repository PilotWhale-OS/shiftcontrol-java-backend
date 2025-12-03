package at.shiftcontrol.shiftservice.entity;

import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
