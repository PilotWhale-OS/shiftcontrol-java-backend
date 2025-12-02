package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Location> locations;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Attendance> attendances;  // TODO only relevant for magament view (not for volunteer) --> not included in volunteer DTOs

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<ShiftPlan> shiftPlans;
}
