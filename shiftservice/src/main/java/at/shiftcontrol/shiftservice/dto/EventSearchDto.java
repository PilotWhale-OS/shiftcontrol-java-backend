package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventSearchDto {
    private String name;
    /**
     *  * The start time of the event search range.
     */
    private Instant startTime;
    private Instant endTime;
}
