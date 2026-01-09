package at.shiftcontrol.lib.event.events.parts;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import at.shiftcontrol.lib.entity.Event;

@Data
public class EventPart {
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

    @JsonCreator
    public EventPart(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("shortDescription") String shortDescription,
        @JsonProperty("longDescription") String longDescription,
        @JsonProperty("startTime") Instant startTime,
        @JsonProperty("endTime") Instant endTime
    ) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @NonNull
    public static EventPart of(@NonNull Event event) {
        return new EventPart(
            String.valueOf(event.getId()),
            event.getName(),
            event.getShortDescription(),
            event.getLongDescription(),
            event.getStartTime(),
            event.getEndTime()
        );
    }
}
