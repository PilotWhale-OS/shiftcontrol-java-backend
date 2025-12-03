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
@Table(name = "activity")
public class Activity {

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

    @Size(max = 1024)
    @Column(nullable = true, length = 1024)
    private String description;

    @ManyToMany(mappedBy = "relatedActivities")
    private Collection<Shift> shifts;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}
