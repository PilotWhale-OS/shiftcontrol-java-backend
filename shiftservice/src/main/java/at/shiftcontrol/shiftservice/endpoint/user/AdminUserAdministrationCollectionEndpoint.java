package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.shiftservice.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserAdministrationCollectionEndpoint {
    private final UserAdministrationService service;

    @GetMapping()
    @Operation(
        operationId = "getAllUsers",
        description = "Find all users."
    )
    public PaginationDto<UserEventDto> getAllUsers(@RequestParam long page, @RequestParam long size) {
        return service.getAllUsers(page, size);
    }

    //todo add bulk endpoint
}
