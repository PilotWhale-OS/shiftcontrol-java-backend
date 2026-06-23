package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteCreateDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteSearchDto;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteAdministrationService;

@Tag(name = "user-invite-endpoint")
@RestController
@RequestMapping(value = "api/v1/users/invites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserInviteCollectionEndpoint {
    private final UserInviteAdministrationService service;

    @GetMapping
    @Operation(
        operationId = "getAllUserInvites",
        description = "Find pending, claimed, revoked, or expired user invites filtered by name or email."
    )
    public PaginationDto<UserInviteDto> getAllInvites(@RequestParam int page, @RequestParam int size, @Valid UserInviteSearchDto searchDto) {
        return service.getAllInvites(page, size, searchDto);
    }

    @PostMapping
    @Operation(
        operationId = "createUserInvite",
        description = "Create a user invite with pending plan access and roles that will be claimed on first login."
    )
    public UserInviteDto createInvite(@RequestBody @Valid UserInviteCreateDto createDto) {
        return service.createInvite(createDto);
    }
}
