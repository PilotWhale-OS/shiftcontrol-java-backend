package at.shiftcontrol.shiftservice.dto.invite;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.event.EventDto;

@Data
@Builder
public class ShiftPlanJoinOverviewDto {
    @NotNull
    @Min(0)
    private int attendingVolunteerCount;

    @NotNull
    private boolean joined;

    @NotNull
    @Valid
    private ShiftPlanInviteDto inviteDto;

    @NotNull
    @Valid
    private EventDto eventDto;
}
