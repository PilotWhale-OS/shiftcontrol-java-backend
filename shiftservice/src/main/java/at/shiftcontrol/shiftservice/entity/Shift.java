package at.shiftcontrol.shiftservice.entity;

import java.time.Instant;
import java.util.Collection;

import jakarta.persistence.CascadeType;
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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_plan_id", nullable = false)
    private ShiftPlan shiftPlan;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 255)
    @Column()
    private String shortDescription;

    @Size(max = 1024)
    @Column(length = 1024)
    private String longDescription;

    @NotNull
    @Column(nullable = false)
    private Instant startTime;

    @NotNull
    @Column(nullable = false)
    private Instant endTime;

    @ManyToOne
    private Location location;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity relatedActivity;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PositionSlot> slots;

    @Override
    public String toString() {
        return "Shift{id=%d, shiftPlan=%d, name='%s', startTime=%s, endTime=%s, location=%s, relatedActivity=%s, slots=%s}"
            .formatted(
                id,
                shiftPlan.getId(),
                name,
                startTime,
                endTime,
                location,
                relatedActivity,
                slots.stream().map(PositionSlot::getId).toList());
    }
}
