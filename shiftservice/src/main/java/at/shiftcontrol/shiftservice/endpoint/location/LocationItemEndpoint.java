package at.shiftcontrol.shiftservice.endpoint.location;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.location.LocationModificationDto;
import at.shiftcontrol.shiftservice.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/locations/{locationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LocationItemEndpoint {
    private final LocationService locationService;

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getLocation",
        description = "Find a location by its id"
    )
    public LocationDto getLocation(@PathVariable String locationId) throws NotFoundException {
        return locationService.getLocation(ConvertUtil.idToLong(locationId));
    }

    @PutMapping()
    // TODO Security
    @Operation(
        operationId = "updateLocation",
        description = "Update a location by its id"
    )
    public LocationDto updateLocation(@PathVariable String locationId, @RequestBody @Valid LocationModificationDto modificationDto)
        throws NotFoundException, ForbiddenException {
        return locationService.updateLocation(ConvertUtil.idToLong(locationId), modificationDto);
    }

    @DeleteMapping()
    // TODO Security
    @Operation(
        operationId = "deleteLocation",
        description = "Delete a location by its id"
    )
    public void deleteLocation(@PathVariable String locationId) throws NotFoundException, ForbiddenException {
        locationService.deleteLocation(ConvertUtil.idToLong(locationId));
    }
}
