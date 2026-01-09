package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.rewardpoints.EventPointsDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsShareTokenCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.RewardPointsShareTokenDto;
import at.shiftcontrol.shiftservice.dto.rewardpoints.TotalPointsDto;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsLedgerService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;
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
@RequestMapping(value = "api/v1/reward-points", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RewardPointsEndpoint {
    private final RewardPointsLedgerService rewardPointsLedgerService;
    private final RewardPointsService rewardPointsService;
    private final ApplicationUserProvider userProvider;

    @GetMapping()
    @Operation(
        operationId = "getCurrentUserTotalRewardPoints",
        description = "Get total reward points of the current user"
    )
    public TotalPointsDto getCurrentUserTotalRewardPoints() throws NotFoundException {
        var currentUser = userProvider.getCurrentUser();
        return rewardPointsLedgerService.getTotalPoints(currentUser.getUserId());
    }

    @GetMapping("/events")
    @Operation(
        operationId = "getCurrentUserEventRewardPoints",
        description = "Get reward points per event for the current user"
    )
    public Collection<EventPointsDto> getCurrentUserEventRewardPoints() throws NotFoundException {
        var currentUser = userProvider.getCurrentUser();
        return rewardPointsLedgerService.getPointsGroupedByEvent(currentUser.getUserId());
    }

    @GetMapping("/events/{eventId}")
    @Operation(
        operationId = "getCurrentUserRewardPointsForEvent",
        description = "Get reward points for a specific event for the current user"
    )
    public EventPointsDto getCurrentUserRewardPointsForEvent(@PathVariable long eventId) throws NotFoundException {
        var currentUser = userProvider.getCurrentUser();
        return rewardPointsLedgerService.getPointsForEvent(currentUser.getUserId(), eventId);
    }

    //    @GetMapping("/share")
//    @DeleteMapping("/share/{shareId}")
//    @GetMapping("/share/{shareToken}")
    @PostMapping("/share")
    @Operation(
        operationId = "createRewardPointsShareToken",
        description = "Create a share token for reward points"
    )
    public RewardPointsShareTokenDto createRewardPointsShareToken(@RequestBody @Valid RewardPointsShareTokenCreateRequestDto requestDto)
        throws NotFoundException {
        return rewardPointsService.createRewardPointsShareToken(requestDto);
    }
}
