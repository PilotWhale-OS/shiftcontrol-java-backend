package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "position_slot_preference")
public class PositionSlotPreference {
    @EmbeddedId
    private PositionSlotPreferenceId id;

    @Size(min = -10, max = 10)
    @Column(nullable = false)
    private int preferenceLevel;
}
