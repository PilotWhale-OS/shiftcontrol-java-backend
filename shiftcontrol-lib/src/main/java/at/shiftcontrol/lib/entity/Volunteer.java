package at.shiftcontrol.lib.entity;

import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "volunteer")
public class Volunteer {
    @Id
    @NotNull
    private String id;

    @ManyToMany
    @JoinTable(
        name = "volunteer_role",
        joinColumns = @JoinColumn(name = "volunteer_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Collection<Role> roles;

    @OneToMany(mappedBy = "volunteerNotificationAssignmentId.volunteerId")
    private Collection<VolunteerNotificationAssignment> notificationAssignments;

    @ManyToMany
    @JoinTable(
        name = "shift_plan_volunteer_volunteering",
        joinColumns = @JoinColumn(name = "volunteer_id"),
        inverseJoinColumns = @JoinColumn(name = "shift_plan_id")
    )
    private Collection<ShiftPlan> volunteeringPlans;

    @ManyToMany
    @JoinTable(
        name = "shift_plan_volunteer_planing",
        joinColumns = @JoinColumn(name = "volunteer_id"),
        inverseJoinColumns = @JoinColumn(name = "shift_plan_id")
    )
    private Collection<ShiftPlan> planningPlans;

    @Override
    public String toString() {
        return "Volunteer{id='%s', roles=%s, notificationAssignments=%s, volunteeringPlans=%s, planningPlans=%s}"
            .formatted(
                id,
                roles,
                notificationAssignments,
                volunteeringPlans.stream().map(ShiftPlan::getId).toList(),
                planningPlans.stream().map(ShiftPlan::getId).toList()
            );
    }
}
