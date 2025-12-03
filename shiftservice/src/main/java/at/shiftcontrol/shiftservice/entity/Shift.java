package at.shiftcontrol.shiftservice.entity;

import at.shiftcontrol.shiftservice.type.LockStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_plan_id", nullable = false)
    private ShiftPlan shiftPlan;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 255)
    @Column(nullable = true, length = 255)
    private String shortDescription;

    @Size(max = 1024)
    @Column(nullable = true, length = 1024)
    private String longDescription;

    @NotNull
    @Column(nullable = false)
    private Instant startTime;

    @NotNull
    @Column(nullable = false)
    private Instant endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_status", nullable = false)
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
