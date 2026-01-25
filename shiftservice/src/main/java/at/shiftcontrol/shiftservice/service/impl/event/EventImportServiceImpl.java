package at.shiftcontrol.shiftservice.service.impl.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.events.EventEvent;
import at.shiftcontrol.lib.exception.ValidationException;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.event.EventExportDto;
import at.shiftcontrol.shiftservice.dto.event.EventImportResultDto;
import at.shiftcontrol.shiftservice.mapper.EventMapper;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.service.event.EventImportService;

@Service
@RequiredArgsConstructor
public class EventImportServiceImpl implements EventImportService {
    private final EventDao eventDao;
    private final ShiftPlanDao shiftPlanDao;
    private final ShiftDao shiftDao;
    private final LocationDao locationDao;
    private final ActivityDao activityDao;

    private final ShiftAssemblingMapper shiftMapper;

    private final ApplicationEventPublisher publisher;

    private static final String SHEET_EVENT = "Event";
    private static final String SHEET_SHIFTS = "Shifts";

    private static final LockStatus DEFAULT_LOCK_STATUS = LockStatus.SELF_SIGNUP;
    private static final int DEFAULT_NO_ROLE_POINTS_PER_MIN = 0;
    private static final int DEFAULT_SHIFT_BONUS_REWARD_POINTS = 0;

    @Override
    @AdminOnly
    @Transactional
    public EventImportResultDto importEvent(@NonNull MultipartFile file) {
        ValidationException.Builder b = ValidationException.builder()
            .context("Import Event (XLSX)");

        if (file.isEmpty()) {
            b.error("file", "Uploaded file is empty.");
        }

        if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".xlsx")) {
            b.error("file", "Invalid file type. Please upload an Excel .xlsx file.");
        }

        b.throwIfInvalid();

        ImportModel model;
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            model = parseWorkbook(wb, b);
        } catch (Exception e) {
            throw ValidationException.builder()
                .context("Import Event (XLSX)")
                .error("file", "Failed to read Excel file: " + e.getMessage())
                .build();
        }

        validateModel(model, b);
        b.throwIfInvalid();

        if (eventDao.existsByNameIgnoreCase(model.eventRow().eventName)) {
            throw ValidationException.builder()
                .context("Import Event (XLSX)")
                .error("Event.event_name", "Event name already exists: '" + model.eventRow().eventName + "'.")
                .build();
        }

        Event event = Event.builder()
            .name(model.eventRow.eventName)
            .startTime(model.eventRow.start)
            .endTime(model.eventRow.end)
            .shortDescription(model.eventRow.shortDescription)
            .longDescription(model.eventRow.longDescription)
            .build();

        event = eventDao.save(event);

        List<Shift> shifts = new ArrayList<>(model.shifts.size());
        Map<String, ShiftPlan> shiftPlanCache = new HashMap<>();
        Map<String, Location> locationCache = new HashMap<>();
        Map<String, Activity> activityCache = new HashMap<>();

        for (ShiftRow r : model.shifts) {
            ShiftPlan shiftPlan;
            if (!shiftPlanCache.containsKey(r.shiftPlanName)) {
                shiftPlan = ShiftPlan.builder()
                    .event(event)
                    .name(r.shiftPlanName)
                    .lockStatus(DEFAULT_LOCK_STATUS)
                    .defaultNoRolePointsPerMinute(DEFAULT_NO_ROLE_POINTS_PER_MIN)
                    .build();
                shiftPlan = shiftPlanDao.save(shiftPlan);
                shiftPlanCache.put(r.shiftPlanName, shiftPlan);
            } else {
                shiftPlan = shiftPlanCache.get(r.shiftPlanName);
            }

            Location location = null;
            if (StringUtils.isNotBlank(r.locationName) && !locationCache.containsKey(r.locationName)) {
                location = Location.builder()
                    .event(event)
                    .name(r.locationName)
                    .readOnly(false)
                    .build();
                location = locationDao.save(location);
                locationCache.put(r.locationName, location);
            } else if (StringUtils.isNotBlank(r.locationName)) {
                location = locationCache.get(r.locationName);
            }

            Activity activity = null;
            if (StringUtils.isNotBlank(r.activityName) && !activityCache.containsKey(r.activityName)) {
                activity = Activity.builder()
                    .event(event)
                    .name(r.activityName)
                    .startTime(r.start)
                    .endTime(r.end)
                    .readOnly(false)
                    .build();
                activity = activityDao.save(activity);
                activityCache.put(r.activityName, activity);
            } else if (StringUtils.isNotBlank(r.activityName)) {
                activity = activityCache.get(r.activityName);
            }

            Shift shift = Shift.builder()
                .shiftPlan(shiftPlan)
                .name(r.shiftName)
                .startTime(r.start)
                .endTime(r.end)
                .shortDescription(r.shortDescription)
                .longDescription(r.longDescription)
                .location(location)
                .relatedActivity(activity)
                .bonusRewardPoints(DEFAULT_SHIFT_BONUS_REWARD_POINTS)
                .build();
            shifts.add(shift);
        }

        shiftDao.saveAll(shifts);

        publisher.publishEvent(EventEvent.eventImported(event));
        return EventImportResultDto.builder()
            .event(EventMapper.toEventDto(event))
            .shifts(shiftMapper.assemble(shifts))
            .build();
    }

    private ImportModel parseWorkbook(Workbook wb, ValidationException.Builder b) {
        Sheet eventSheet = wb.getSheet(SHEET_EVENT);
        if (eventSheet == null) {
            b.error("file", "Missing sheet '" + SHEET_EVENT + "'.");
        }

        Sheet shiftsSheet = wb.getSheet(SHEET_SHIFTS);
        if (shiftsSheet == null) {
            b.error("file", "Missing sheet '" + SHEET_SHIFTS + "'.");
        }

        b.throwIfInvalid();

        EventRow eventRow = parseEventSheet(eventSheet, b);
        List<ShiftRow> shiftRows = parseShiftsSheet(shiftsSheet, b);
        b.throwIfInvalid();

        return new ImportModel(eventRow, shiftRows);
    }

    private EventRow parseEventSheet(Sheet sheet, ValidationException.Builder b) {
        Map<String, Integer> header = readHeader(sheet, 0);

        String eventName = readStringCell(sheet, 2, header, "event_name", b, true);
        Instant start = readInstantCell(sheet, 2, header, "start_time_utc", b, true);
        Instant end = readInstantCell(sheet, 2, header, "end_time_utc", b, true);
        String shortDescription = readStringCell(sheet, 2, header, "short_description", b, false);
        String longDescription = readStringCell(sheet, 2, header, "long_description", b, false);

        return new EventRow(normalizeName(eventName), start, end, shortDescription, longDescription);
    }

    private List<ShiftRow> parseShiftsSheet(Sheet sheet, ValidationException.Builder b) {
        Map<String, Integer> header = readHeader(sheet, 0);

        int lastRow = sheet.getLastRowNum();
        List<ShiftRow> shifts = new ArrayList<>();

        for (int r = 2; r <= lastRow; r++) {
            // skip completely empty rows
            if (isRowEmpty(sheet.getRow(r))) {
                continue;
            }

            String shiftPlanName = readStringCell(sheet, r, header, "shift_plan_name", b, false);
            String name = readStringCell(sheet, r, header, "shift_name", b, true);
            Instant start = readInstantCell(sheet, r, header, "start_time_utc", b, true);
            Instant end = readInstantCell(sheet, r, header, "end_time_utc", b, true);
            String locationName = readStringCell(sheet, r, header, "location_name", b, false);
            String activityName = readStringCell(sheet, r, header, "activity_name", b, false);
            String shortDescription = readStringCell(sheet, r, header, "short_description", b, false);
            String longDescription = readStringCell(sheet, r, header, "long_description", b, false);

            shifts.add(new ShiftRow(
                normalizeName(shiftPlanName),
                normalizeName(name),
                start,
                end,
                normalizeName(locationName),
                normalizeName(activityName),
                shortDescription,
                longDescription,
                r + 1 // excel row number
            ));
        }

        if (shifts.isEmpty()) {
            b.error("file", "Sheet '" + sheet.getSheetName() + "' contains no shift entries.");
        }

        return shifts;
    }

    private Map<String, Integer> readHeader(Sheet sheet, int headerRowIndex) {
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) {
            throw ValidationException.builder()
                .context("Import Event (XLSX)")
                .error("file", "Sheet '" + sheet.getSheetName() + "' is missing header row at index " + (headerRowIndex + 1) + ".")
                .build();
        }

        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String raw = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell);
            String key = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if (!key.isBlank()) {
                map.put(key, cell.getColumnIndex());
            }
        }
        return map;
    }

    private String readStringCell(Sheet sheet, int rowIndex, Map<String, Integer> header,
                                  String column, ValidationException.Builder b, boolean required) {
        Integer colIndex = header.get(column);
        if (colIndex == null) {
            b.error("file", "Missing required column '" + column + "' in sheet '" + sheet.getSheetName() + "'.");
            return null;
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            if (required) {
                b.error("file", "Sheet '" + sheet.getSheetName() + "', row " + (rowIndex + 1) +
                    ": '" + column + "' is required.");
            }
            return null;
        }

        Cell cell = row.getCell(colIndex);
        String value = cellToString(cell);

        if (required && (value == null || value.trim().isEmpty())) {
            b.error("file", "Sheet '" + sheet.getSheetName() + "', row " + (rowIndex + 1) +
                ": '" + column + "' is required.");
        }

        return value == null ? null : value.trim();
    }

    private Instant readInstantCell(Sheet sheet, int rowIndex, Map<String, Integer> header,
                                    String column, ValidationException.Builder b, boolean required) {
        String s = readStringCell(sheet, rowIndex, header, column, b, required);
        if (s == null || s.isBlank()) {
            return null;
        }

        s = s.trim();

        // Normalize common variants
        if (s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}Z")) {
            // add seconds
            s = s.replace("Z", ":00Z");
        }

        if (s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
            // assume UTC
            s = s + ":00Z";
        }

        try {
            return Instant.parse(s);
        } catch (Exception e) {
            b.error("file", "Sheet '" + sheet.getSheetName() + "', row " + (rowIndex + 1) +
                ": '" + column + "' has invalid datetime format: '" + s + "'. Expected ISO 8601 format.");
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String s = cellToString(cell);
                if (s != null && !s.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toString()
                : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula(); // expects user to input raw values, not formulas
            default -> null;
        };
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        // trim + collapse whitespace + lowercase for comparisons elsewhere
        return name.trim().replaceAll("\\s+", " ");
    }

    private void validateModel(ImportModel model, ValidationException.Builder b) {
        if (model.eventRow().start != null && model.eventRow.end != null && !model.eventRow.start.isBefore(model.eventRow().end)) {
            b.error("Event.start_time_utc", "Event start_time_utc must be before end_time_utc.");
        }

        // Duplicate shift_name is only forbidden within the same shift plan
        Map<String, List<ShiftRow>> byPlanAndShift = model.shifts().stream()
            .collect(Collectors.groupingBy(s ->
                normalizeKey(s.shiftPlanName()) + "||" + normalizeKey(s.shiftName())
            ));

        for (var entry : byPlanAndShift.entrySet()) {
            List<ShiftRow> rows = entry.getValue();
            if (rows.size() <= 1) {
                continue;
            }

            ShiftRow first = rows.get(0);
            String plan = first.shiftPlanName();
            String shift = first.shiftName();

            String rowNums = rows.stream()
                .map(r -> String.valueOf(r.excelRow()))
                .collect(Collectors.joining(", "));

            b.error(
                "Shifts.shift_name",
                "Duplicate shift_name '" + shift + "' in shift_plan '" + plan + "' (rows: " + rowNums + ")."
            );
        }

        for (ShiftRow s : model.shifts) {
            if (s.start != null && s.end != null && !s.start.isBefore(s.end)) {
                b.error("Shifts.start_time_utc", "Sheet 'Shifts', row " + s.excelRow + ": start_time_utc must be before end_time_utc.");
            }

            if (StringUtils.isNotBlank(s.locationName) && StringUtils.isNotBlank(s.activityName)) {
                b.error("Shifts.location_name", "Sheet 'Shifts', row " + s.excelRow + ": Cannot set both location_name and activity_name.");
            }

            // Enforce within event range
            if (model.eventRow.start != null && s.start != null && s.start.isBefore(model.eventRow.start)) {
                b.error("Shifts.start_time_utc", "Sheet 'Shifts', row " + s.excelRow + ": shift starts before event start.");
            }
            if (model.eventRow.end != null && s.end != null && s.end.isAfter(model.eventRow.end)) {
                b.error("Shifts.end_time_utc", "Sheet 'Shifts', row " + s.excelRow + ": shift ends after event end.");
            }
        }
    }

    private static String normalizeKey(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record ImportModel(EventRow eventRow, List<ShiftRow> shifts) {
    }

    private record EventRow(String eventName, Instant start, Instant end, String shortDescription, String longDescription) {
    }

    private record ShiftRow(String shiftPlanName, String shiftName, Instant start, Instant end, String locationName, String activityName,
                            String shortDescription, String longDescription, int excelRow) {
    }

    @Override
    public EventExportDto getEventImportTemplate() {
        var mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        try {
            InputStream templateStream = this.getClass().getResourceAsStream("/templates/event_import_template.xlsx");
            if (templateStream == null) {
                throw new FileNotFoundException("Event import template not found.");
            }

            return EventExportDto.builder()
                .exportStream(templateStream)
                .fileName("event_import_template.xlsx")
                .mediaType(MediaType.parseMediaType(mediaType))
                .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load event import template: " + e.getMessage(), e);
        }
    }
}
