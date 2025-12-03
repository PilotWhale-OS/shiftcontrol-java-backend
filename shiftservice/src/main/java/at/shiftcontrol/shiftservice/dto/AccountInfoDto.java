package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfoDto {
    @NotNull
    private String id;
    @NotNull
    private String firstName; // TODO check if this needs to be tracked here
    @NotNull
    private String lastName;
    @NotNull
    private String email;
}
