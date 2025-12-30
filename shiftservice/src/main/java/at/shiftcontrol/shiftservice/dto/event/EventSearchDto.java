package at.shiftcontrol.shiftservice.dto.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchDto {
    private String name;
    /**
     * * The start time of the event search range.
     */
    private Instant startTime;
    private Instant endTime;
}
