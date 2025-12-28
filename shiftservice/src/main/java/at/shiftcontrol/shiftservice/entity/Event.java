package at.shiftcontrol.shiftservice.entity;

import java.time.Instant;
import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;
    @Size(max = 255)
    @Column(nullable = true, length = 255)
    private String shortDescription;
    @Size(max = 1025)
    @Column(nullable = true, length = 1024)
    private String longDescription;
    @NotNull
    @Column(nullable = false)
    private Instant startTime;
    @NotNull
    @Column(nullable = false)
    private Instant endTime;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Location> locations;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Attendance> attendances;  // TODO only relevant for magament view (not for volunteer) --> not included in volunteer DTOs
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<ShiftPlan> shiftPlans;

    @Override
    public String toString() {
        return "Event{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", locations=" + locations.stream().map(Location::getId).toList() +
            ", attendances=" + attendances +
            ", shiftPlans=" + shiftPlans.stream().map(ShiftPlan::getId).toList() +
            '}';
    }
}
