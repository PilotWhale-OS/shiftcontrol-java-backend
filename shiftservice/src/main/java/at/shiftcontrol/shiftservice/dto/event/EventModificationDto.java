package at.shiftcontrol.shiftservice.dto.event;

import java.time.LocalDate;

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
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
}
