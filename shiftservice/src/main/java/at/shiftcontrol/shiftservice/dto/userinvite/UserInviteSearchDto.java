package at.shiftcontrol.shiftservice.dto.userinvite;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.lib.type.UserInviteStatus;

@Data
@Builder
public class UserInviteSearchDto {
    private String name;
    private UserInviteStatus status;
}
