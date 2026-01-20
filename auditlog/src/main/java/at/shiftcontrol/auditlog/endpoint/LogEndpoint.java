package at.shiftcontrol.auditlog.endpoint;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import at.shiftcontrol.auditlog.dto.LogEntryDto;
import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.service.AuditLogService;

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
    public List<LogEntryDto> getLogs(@RequestBody LogSearchDto searchDto) {
        return LogEntryDto.of(auditLogService.search(searchDto));
    }
}
