package at.shiftcontrol.shiftsystem.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
public class Location {
    private long id;
    @NonNull
    private Event event;

    private String name;
    private String description;
    private String url;

    private Map<String, Object> additionalProperties;
}
