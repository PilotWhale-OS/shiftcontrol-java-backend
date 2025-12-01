package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UnavailabilityDto {
    @NotNull
    private String id;

    @NotNull
    private Instant from;

    @NotNull
    private Instant to;
}
