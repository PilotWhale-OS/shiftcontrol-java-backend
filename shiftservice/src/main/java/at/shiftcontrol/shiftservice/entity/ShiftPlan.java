package at.shiftcontrol.shiftservice.entity;

import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        return "ShiftPlan{" +
            "id=" + id +
            ", event=" + event.getId() +
            ", name='" + name + '\'' +
            ", shifts=" + shifts.stream().map(Shift::getId).toList() +
            ", planVolunteers=" + planVolunteers.stream().map(Volunteer::getId).toList() +
            ", planPlanners=" + planPlanners.stream().map(Volunteer::getId).toList() +
            '}';
    }
}
