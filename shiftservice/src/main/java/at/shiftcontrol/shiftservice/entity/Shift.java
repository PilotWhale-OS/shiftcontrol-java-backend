package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.LockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_plan_id", nullable = false)
    private ShiftPlan shiftPlan;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String shortDescription;

    @Column(nullable = true)
    private String longDescription;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_status", nullable = true)
    private LockStatus lockStatus;

    @ManyToMany
    @JoinTable(
            name = "shift_location",
            joinColumns = @JoinColumn(name = "shift_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Collection<Location> locations; // TODO flo patrick remove if locations are moved to PositionSlot

    @ManyToMany
    @JoinTable(
            name = "activity_shift",
            joinColumns = @JoinColumn(name = "shift_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    private Collection<Activity> relatedActivities;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PositionSlot> slots;
}
