package at.shiftcontrol.shiftservice.dto;

import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeInfoDto {
    @NotNull
    private String offeredPositionSlotId;

    @NotNull
    private String requestedPositionSlotId;

    @NotNull
    @Valid
    private VolunteerDto offeringVolunteer;

    @NotNull
    private TradeStatus status;

    @NotNull
    private Instant createdAt;
}
