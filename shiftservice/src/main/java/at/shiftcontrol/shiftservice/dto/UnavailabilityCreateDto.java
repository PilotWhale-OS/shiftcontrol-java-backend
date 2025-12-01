package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UnavailabilityCreateDto {
    @NotNull
    private Instant from;

    @NotNull
    private Instant to;
}
