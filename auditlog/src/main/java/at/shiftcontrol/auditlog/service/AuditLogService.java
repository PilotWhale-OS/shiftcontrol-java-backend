package at.shiftcontrol.auditlog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.auditlog.dao.specification.LogEntrySpecifications;
import at.shiftcontrol.auditlog.dto.LogEntryCreateDto;
import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.entity.LogEntry;
import at.shiftcontrol.auditlog.repo.LogEntryRepository;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final LogEntryRepository logEntryRepository;

    public LogEntry create(LogEntryCreateDto createDto) {
        var entry = LogEntry.builder()
                .id(UUID.randomUUID())
                .routingKey(createDto.getRoutingKey())
                .actingUserId(createDto.getActingUserId())
                .traceId(createDto.getTraceId())
                .timestamp(createDto.getTimestamp())
                .payload(createDto.getPayload().toString())
                .build();
        return logEntryRepository.save(entry);
    }

    public List<LogEntry> search(LogSearchDto searchDto) {
        return logEntryRepository.findAll(LogEntrySpecifications.matchesSearchDto(searchDto));
    }
}
