package at.shiftcontrol.shiftservice.dto.activity;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityModificationDto {
    @NotNull
    private String name;
    private String description;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    private String locationId;
}
