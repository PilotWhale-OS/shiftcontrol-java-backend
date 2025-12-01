package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Data
@Builder
public class EventOverviewDto {
    @NotNull
    private String id;

    @NotNull
    private String name;
    
    private String shortDescription;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;
}
