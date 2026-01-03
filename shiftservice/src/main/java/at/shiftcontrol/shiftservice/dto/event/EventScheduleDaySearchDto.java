package at.shiftcontrol.shiftservice.dto.event;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleDaySearchDto {
    @NotNull
    private LocalDate date;
}
