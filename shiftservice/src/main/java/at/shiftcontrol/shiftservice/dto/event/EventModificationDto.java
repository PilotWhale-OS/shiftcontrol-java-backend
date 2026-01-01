package at.shiftcontrol.shiftservice.dto.event;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventModificationDto {
    @NotNull
    private String name;

    private String shortDescription;

    private String longDescription;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;
}
