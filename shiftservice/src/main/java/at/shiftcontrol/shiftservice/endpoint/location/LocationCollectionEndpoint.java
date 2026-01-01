package at.shiftcontrol.shiftservice.endpoint.location;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;
import at.shiftcontrol.shiftservice.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/locations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LocationCollectionEndpoint {
    private final LocationService locationService;

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getAllLocationsForEvent",
        description = "Find all locations for a specific event"
    )
    public Collection<LocationDto> getAllLocationsForEvent(@PathVariable long eventId) throws NotFoundException, ForbiddenException {
        return locationService.getAllLocationsForEvent(eventId);
    }

    @PostMapping()
    // TODO Security
    @Operation(
        operationId = "createLocation",
        description = "Create a new location for a specific event"
    )
    public LocationDto createLocation(@PathVariable long eventId, @RequestBody @Valid LocationModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        return locationService.createLocation(eventId, modificationDto);
    }
}
