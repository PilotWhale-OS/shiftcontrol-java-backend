package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCandidatesDto {
    @NotNull
    private String positionSlotId;
    @NotNull
    private Collection<AccountInfoDto> assignedVolunteers;
}
