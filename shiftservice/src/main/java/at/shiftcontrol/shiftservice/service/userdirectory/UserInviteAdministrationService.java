package at.shiftcontrol.shiftservice.service.userdirectory;

import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteCreateDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteSearchDto;

public interface UserInviteAdministrationService {
    @NonNull PaginationDto<UserInviteDto> getAllInvites(int page, int size, @NonNull UserInviteSearchDto searchDto);

    @NonNull UserInviteDto createInvite(@NonNull UserInviteCreateDto createDto);

    void revokeInvite(long inviteId);
}
