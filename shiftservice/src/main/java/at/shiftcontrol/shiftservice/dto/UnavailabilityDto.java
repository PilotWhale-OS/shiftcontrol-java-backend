package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

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
