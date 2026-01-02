package at.shiftcontrol.shiftservice.endpoint.activity;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityModificationDto;
import at.shiftcontrol.shiftservice.service.ActivityService;
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
@RequestMapping(value = "api/v1/activities/{activityId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ActivityItemEndpoint {
    private final ApplicationUserProvider userProvider;
    private final ActivityService activityService;

    // TODO: Get single, update, delete

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getActivity",
        description = "Find activity by id"
    )
    public ActivityDto getActivity(@PathVariable String activityId) throws NotFoundException {
        return activityService.getActivity(ConvertUtil.idToLong(activityId));
    }

    @PutMapping()
    // TODO Security
    @Operation(
        operationId = "updateActivity",
        description = "Update activity by id"
    )
    public ActivityDto updateActivity(@PathVariable String activityId, @RequestBody @Valid ActivityModificationDto modificationDto) throws NotFoundException {
        return activityService.updateActivity(ConvertUtil.idToLong(activityId), modificationDto);
    }

    @DeleteMapping()
    // TODO Security
    @Operation(
        operationId = "deleteActivity",
        description = "Delete activity by id"
    )
    public void deleteActivity(@PathVariable String activityId) throws NotFoundException {
        activityService.deleteActivity(ConvertUtil.idToLong(activityId));
    }

}
