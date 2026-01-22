package at.shiftcontrol.auditlog.endpoint;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.auditlog.dto.LogEntryDto;
import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.service.AuditLogService;
import at.shiftcontrol.lib.dto.PaginationDto;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/log", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LogEndpoint {
    private final AuditLogService auditLogService;

    @GetMapping
    @Secured("ADMIN")
    @Operation(
        operationId = "getLogs",
        description = "Search for auditlog entries"
    )
    public PaginationDto<LogEntryDto> getLogs(@RequestParam int page, @RequestParam int size, @Valid LogSearchDto searchDto) {
        return auditLogService.search(page, size, searchDto);
    }
}
