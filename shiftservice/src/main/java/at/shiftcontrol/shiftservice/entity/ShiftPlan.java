package at.shiftcontrol.shiftservice.entity;

import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

import at.shiftcontrol.shiftservice.type.LockStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
    @Column(nullable = false)
    private String name;

    @Size(max = 255)
    @Column()
    private String shortDescription;

    @Size(max = 1024)
    @Column(length = 1024)
    private String longDescription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_status", nullable = false)
    private LockStatus lockStatus;

    @OneToMany(mappedBy = "shiftPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Shift> shifts;

    @ManyToMany
    @JoinTable(
        name = "shift_plan_volunteer_volunteering",
        joinColumns = @JoinColumn(name = "shift_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "volunteer_id")
    )
    private Collection<Volunteer> planVolunteers;

    @ManyToMany
    @JoinTable(
        name = "shift_plan_volunteer_planing",
        joinColumns = @JoinColumn(name = "shift_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "volunteer_id")
    )
    private Collection<Volunteer> planPlanners;

    @Override
    public String toString() {
        return "ShiftPlan{id=%d, event=%d, name='%s', shifts=%s, planVolunteers=%s, planPlanners=%s}"
            .formatted(
                id,
                event.getId(),
                name,
                shifts.stream().map(Shift::getId).toList(),
                planVolunteers.stream().map(Volunteer::getId).toList(),
                planPlanners.stream().map(Volunteer::getId).toList());
    }
}
