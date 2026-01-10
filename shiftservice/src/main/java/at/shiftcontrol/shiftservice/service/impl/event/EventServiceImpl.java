package at.shiftcontrol.shiftservice.service.impl.event;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.EventEvent;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.FileExportException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.type.ExportFormat;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.RewardPointsTransactionDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventExportDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftPlanMapper;
import at.shiftcontrol.shiftservice.service.StatisticService;
import at.shiftcontrol.shiftservice.service.event.EventService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final ActivityDao activityDao;
    private final VolunteerDao volunteerDao;
    private final RewardPointsTransactionDao rewardPointsTransactionDao;
    private final StatisticService statisticService;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public EventDto getEvent(long eventId) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventDto> search(EventSearchDto searchDto) {
        var filteredEvents = eventDao.search(searchDto);
        var currentUser = userProvider.getCurrentUser();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin(currentUser)) {
            return EventMapper.toEventDto(filteredEvents);
        }
        String userId = currentUser.getUserId();

        var volunteer = volunteerDao.getById(userId);
        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter events that the volunteer is part of
        var relevantEvents = filteredEvents.stream()
            .filter(event -> event.getShiftPlans().stream()
                .anyMatch(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            )
            .toList();

        return EventMapper.toEventDto(relevantEvents);
    }

    @Override
    public List<ShiftPlanDto> getUserRelatedShiftPlansOfEvent(long eventId, String userId) {
        return ShiftPlanMapper.toShiftPlanDto(getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId));
    }

    @Override
    public EventShiftPlansOverviewDto getEventShiftPlansOverview(long eventId, String userId) {
        var event = eventDao.getById(eventId);

        var eventOverviewDto = EventMapper.toEventDto(event);
        var userRelevantShiftPlans = getUserRelatedShiftPlanEntitiesOfEvent(eventId, userId);

        return EventShiftPlansOverviewDto.builder()
            .eventOverview(eventOverviewDto)
            .shiftPlans(ShiftPlanMapper.toShiftPlanDto(userRelevantShiftPlans))
            .rewardPoints((int) rewardPointsTransactionDao.sumPointsByVolunteerAndEvent(userId, eventId))
            .ownEventStatistics(statisticService.getOwnStatisticsOfShiftPlans(userRelevantShiftPlans, userId))
            .overallEventStatistics(statisticService.getOverallEventStatistics(event))
            .build();
    }

    @Override
    public EventScheduleDto getEventSchedule(long eventId, EventScheduleDaySearchDto searchDto) {
        var event = eventDao.getById(eventId);
        securityHelper.assertUserIsAllowedToAccessEvent(event);

        var activitiesOfEvent = activityDao.searchActivitiesInEvent(eventId, searchDto).stream().toList();

        return EventMapper.toEventScheduleDto(event, activitiesOfEvent);
    }

    @Override
    @AdminOnly
    public EventDto createEvent(@NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = EventMapper.toEvent(modificationDto);
        event = eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.EVENT_CREATED, event));
        return EventMapper.toEventDto(event);
    }

    @Override
    @AdminOnly
    public EventDto updateEvent(long eventId, @NonNull EventModificationDto modificationDto) {
        validateEventModificationDto(modificationDto);

        Event event = eventDao.getById(eventId);
        EventMapper.updateEvent(event, modificationDto);
        eventDao.save(event);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_UPDATED, Map.of("eventId", String.valueOf(eventId))), event));
        return EventMapper.toEventDto(event);
    }

    private void validateEventModificationDto(EventModificationDto modificationDto) {
        if (modificationDto.getStartTime().isAfter(modificationDto.getEndTime())) {
            throw new BadRequestException("Event end time must be after start time");
        }
    }

    @Override
    @AdminOnly
    public void deleteEvent(long eventId) {
        var event = eventDao.getById(eventId);

        publisher.publishEvent(EventEvent.of(RoutingKeys.format(RoutingKeys.EVENT_DELETED, Map.of("eventId", String.valueOf(eventId))), event));
        eventDao.delete(event);
    }

    private List<ShiftPlan> getUserRelatedShiftPlanEntitiesOfEvent(long eventId, String userId) {
        var event = eventDao.getById(eventId);
        var shiftPlans = event.getShiftPlans();

        // skip filtering for admin users
        if (securityHelper.isUserAdmin()) {
            return shiftPlans.stream().toList();
        }

        var volunteer = volunteerDao.getById(userId);

        var volunteerShiftPlans = volunteer.getVolunteeringPlans();
        var planningShiftPlans = volunteer.getPlanningPlans();

        // filter shiftPlans that the volunteer is part of (volunteerShiftPlans)
        return shiftPlans.stream()
            .filter(shiftPlan -> volunteerShiftPlans.contains(shiftPlan) || planningShiftPlans.contains(shiftPlan))
            .toList();
    }

    @Override
    @AdminOnly
    public EventExportDto exportEvent(long eventId, String format) {
        var exportFormat = ExportFormat.fromString(format);
        if (exportFormat == null) {
            throw new BadRequestException("Invalid format: " + format + ". Supported formats are: "
                + Stream.of(ExportFormat.VALUES).map(Enum::name).collect(Collectors.joining(", ")));
        }
        var event = eventDao.getById(eventId);

        var exportData = getExportData(event);

        var formattedEventName = event.getName().replaceAll("\\s+", "_");
        ByteArrayOutputStream out = switch (exportFormat) {
            case CSV -> exportToCsv(exportData);
            case XLSX -> exportToExcel(exportData, "Event_" + formattedEventName);
        };

        var mediaType = MediaType.parseMediaType(switch (exportFormat) {
            case CSV -> "application/csv";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        });

        return EventExportDto.builder()
            .exportStream(new ByteArrayInputStream(out.toByteArray()))
            .fileName("Event_" + formattedEventName + "." + exportFormat.name().toLowerCase())
            .mediaType(mediaType)
            .build();
    }

    private List<Map<String, Object>> getExportData(Event event) {
        var shiftPlans = event.getShiftPlans() == null ? List.<ShiftPlan>of() : event.getShiftPlans();

        var shifts = shiftPlans.stream()
            .filter(Objects::nonNull)
            .flatMap(sp -> sp.getShifts() == null ? Stream.empty() : sp.getShifts().stream())
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Shift::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        String eventName = event.getName();
        String eventStart = event.getStartTime().toString();
        String eventEnd = event.getEndTime().toString();

        List<Map<String, Object>> rows = new ArrayList<>(Math.max(1, shifts.size()));

        for (Shift shift : shifts) {
            int required = resolveRequiredSlots(shift);
            int filled = resolveFilledSlots(shift);
            int open = Math.max(0, required - filled);

            var row = new LinkedHashMap<String, Object>();

            row.put("event_name", eventName);
            row.put("event_start_time_utc", eventStart);
            row.put("event_end_time_utc", eventEnd);

            row.put("shift_name", shift.getName());
            row.put("shift_start_time_utc", shift.getStartTime());
            row.put("shift_end_time_utc", shift.getEndTime());

            var location = shift.getLocation();
            row.put("shift_location", location == null ? "" : location.getName());

            var relatedActivity = shift.getRelatedActivity();
            row.put("shift_related_activity", relatedActivity == null ? "" : relatedActivity.getName());

            row.put("shift_required_volunteers_total", required);
            row.put("shift_filled_volunteers_total", filled);
            row.put("shift_open_volunteers_total", open);

            rows.add(row);
        }

        // If there are no shifts, still export a single row with event info
        if (rows.isEmpty()) {
            var row = new LinkedHashMap<String, Object>();
            row.put("event_name", eventName);
            row.put("event_start_time_utc", eventStart);
            row.put("event_end_time_utc", eventEnd);
            row.put("name", "");
            row.put("start_time_utc", "");
            row.put("end_time_utc", "");
            row.put("location", "");
            row.put("related_activity", "");
            row.put("required_total", 0);
            row.put("filled_total", 0);
            row.put("open_total", 0);
            row.put("notes", "");
            rows.add(row);
        }

        return rows;
    }

    private int resolveRequiredSlots(Shift shift) {
        return shift.getSlots().stream()
            .mapToInt(PositionSlot::getDesiredVolunteerCount)
            .sum();
    }

    private int resolveFilledSlots(Shift shift) {
        return (int) shift.getSlots().stream()
            .flatMap(slot -> slot.getAssignments().stream())
            .filter(assignment -> !AssignmentStatus.REQUEST_FOR_ASSIGNMENT.equals(assignment.getStatus()))
            .count();
    }

    private ByteArrayOutputStream exportToCsv(List<Map<String, Object>> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            List<String> headers = resolveHeaders(rows);

            osw.write('\uFEFF');

            // Header
            writer.println(headers.stream()
                .map(this::csvEscape)
                .collect(Collectors.joining(",")));

            // Rows
            for (Map<String, Object> row : rows) {
                String line = headers.stream()
                    .map(h -> csvEscape(formatCellValue(row.get(h))))
                    .collect(Collectors.joining(","));
                writer.println(line);
            }

            writer.flush();
            return out;
        } catch (Exception e) {
            throw new FileExportException("Error generating CSV file", e);
        }
    }

    private List<String> resolveHeaders(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> headers = new LinkedHashSet<>(rows.get(0).keySet());
        for (Map<String, Object> row : rows) {
            headers.addAll(row.keySet());
        }
        return new ArrayList<>(headers);
    }

    private String formatCellValue(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof String s) {
            return s;
        }
        if (v instanceof Boolean b) {
            return b ? "true" : "false";
        }
        if (v instanceof Integer || v instanceof Long || v instanceof Short) {
            return String.valueOf(v);
        }
        if (v instanceof Float || v instanceof Double) {
            return String.valueOf(v);
        }
        if (v instanceof BigDecimal bd) {
            return bd.toPlainString();
        }

        if (v instanceof Instant i) {
            return i.toString();
        }
        if (v instanceof LocalDateTime ldt) {
            return ldt.toString();
        }
        if (v instanceof OffsetDateTime odt) {
            return odt.toString();
        }
        if (v instanceof ZonedDateTime zdt) {
            return zdt.toString();
        }

        return String.valueOf(v);
    }

    private String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!mustQuote) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private ByteArrayOutputStream exportToExcel(List<Map<String, Object>> rows, String sheetName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             XSSFWorkbook workbook = new XSSFWorkbook()) {

            String safeSheetName = WorkbookUtil.createSafeSheetName(
                sheetName == null || sheetName.isBlank() ? "Event_Export" : sheetName
            );
            Sheet sheet = workbook.createSheet(safeSheetName);

            List<String> headers = resolveHeaders(rows);

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            DataFormat df = workbook.createDataFormat();
            CellStyle integerStyle = workbook.createCellStyle();
            integerStyle.setDataFormat(df.getFormat("0"));

            CellStyle decimalStyle = workbook.createCellStyle();
            decimalStyle.setDataFormat(df.getFormat("0.00"));

            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm"));

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers.get(i));
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            for (Map<String, Object> rowMap : rows) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    Object value = rowMap.get(header);

                    Cell cell = row.createCell(c);
                    writeExcelCell(cell, value, integerStyle, decimalStyle, dateTimeStyle);
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        } catch (Exception e) {
            throw new FileExportException("Error generating Excel file", e);
        }
    }

    private void writeExcelCell(
        Cell cell,
        Object value,
        CellStyle integerStyle,
        CellStyle decimalStyle,
        CellStyle dateTimeStyle
    ) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof Integer i) {
            cell.setCellValue(i.doubleValue());
            cell.setCellStyle(integerStyle);
            return;
        }
        if (value instanceof Long l) {
            cell.setCellValue(l.doubleValue());
            cell.setCellStyle(integerStyle);
            return;
        }
        if (value instanceof Double d) {
            cell.setCellValue(d);
            cell.setCellStyle(decimalStyle);
            return;
        }
        if (value instanceof Float f) {
            cell.setCellValue(f.doubleValue());
            cell.setCellStyle(decimalStyle);
            return;
        }
        if (value instanceof BigDecimal bd) {
            cell.setCellValue(bd.doubleValue());
            cell.setCellStyle(decimalStyle);
            return;
        }
        if (value instanceof Boolean b) {
            cell.setCellValue(b);
            return;
        }

        if (value instanceof Instant instant) {
            cell.setCellValue(Date.from(instant));
            cell.setCellStyle(dateTimeStyle);
            return;
        }
        if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateTimeStyle);
            return;
        }
        if (value instanceof OffsetDateTime odt) {
            cell.setCellValue(Date.from(odt.toInstant()));
            cell.setCellStyle(dateTimeStyle);
            return;
        }
        if (value instanceof ZonedDateTime zdt) {
            cell.setCellValue(Date.from(zdt.toInstant()));
            cell.setCellStyle(dateTimeStyle);
            return;
        }

        // Fallback
        cell.setCellValue(String.valueOf(value));
    }
}
