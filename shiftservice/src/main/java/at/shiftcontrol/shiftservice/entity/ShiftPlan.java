package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_plan")
public class ShiftPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

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

    @OneToMany(mappedBy = "shiftPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Shift> shifts;

    @ManyToMany
    @JoinTable(
            name = "shift_plan_volunteer",
            joinColumns = @JoinColumn(name = "shift_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "volunteer_id")
    )
    private Collection<Volunteer> participatingVolunteers;
}
