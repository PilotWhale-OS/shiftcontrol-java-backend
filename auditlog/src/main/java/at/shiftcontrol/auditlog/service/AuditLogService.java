package at.shiftcontrol.auditlog.service;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.auditlog.dao.specification.LogEntrySpecifications;
import at.shiftcontrol.auditlog.dto.LogEntryCreateDto;
import at.shiftcontrol.auditlog.dto.LogEntryDto;
import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.entity.LogEntry;
import at.shiftcontrol.auditlog.repo.LogEntryRepository;
import at.shiftcontrol.lib.dto.PaginationDto;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final LogEntryRepository logEntryRepository;

    public LogEntry create(LogEntryCreateDto createDto) {
        var entry = LogEntry.builder()
                .id(UUID.randomUUID())
                .routingKey(createDto.getRoutingKey())
                .eventType(createDto.getEventType())
                .description(createDto.getDescription())
                .actingUserId(createDto.getActingUserId())
                .traceId(createDto.getTraceId())
                .timestamp(createDto.getTimestamp())
                .payload(createDto.getPayload().toString())
                .build();
        return logEntryRepository.save(entry);
    }

    public PaginationDto<LogEntryDto> search(int page, int size, LogSearchDto searchDto) {
        var entries = logEntryRepository.findAll(LogEntrySpecifications.matchesSearchDto(searchDto), PageRequest.of(page, size));
        return PaginationDto.<LogEntryDto>builder()
            .page(page)
            .pages(entries.getTotalPages())
            .total((int) entries.getTotalElements())
            .items(LogEntryDto.of(entries.getContent()))
            .build();
    }
}
