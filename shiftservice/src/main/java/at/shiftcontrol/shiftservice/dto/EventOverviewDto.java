package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventOverviewDto {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
}
