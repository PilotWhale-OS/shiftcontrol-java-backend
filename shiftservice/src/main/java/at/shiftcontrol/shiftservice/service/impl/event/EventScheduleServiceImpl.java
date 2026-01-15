package at.shiftcontrol.shiftservice.service.impl.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.type.PositionSignupState;
import at.shiftcontrol.lib.type.ShiftRelevance;
import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.shift.ShiftColumnDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleLayoutDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleContentNoLocationDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleLayoutDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ScheduleLayoutNoLocationDto;
import at.shiftcontrol.shiftservice.mapper.ActivityMapper;
import at.shiftcontrol.shiftservice.mapper.LocationMapper;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.service.event.EventScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventScheduleServiceImpl implements EventScheduleService {
    private final EventDao eventDao;

    @Override
    public EventScheduleLayoutDto getEventScheduleLayout(long eventId, EventScheduleFilterDto filterDto) {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, filterDto);
        // Build location DTOs
        var scheduleLayoutDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleLayoutDto(entry.getKey(), entry.getValue()))
            .toList();

        var shiftsWithoutLocation = shiftDao.searchShiftsInShiftPlan(shiftPlanId,
                userProvider.getCurrentUser().getUserId(),
                filterDto).stream()
            .filter(shift -> shift.getLocation() == null)
            .toList();
        var scheduleLayoutNoLocationDto = buildScheduleLayoutNoLocationDto(shiftsWithoutLocation);

        var stats = statisticService.getShiftPlanScheduleStatistics(shiftsByLocation.values().stream().flatMap(List::stream).toList());
        return EventScheduleLayoutDto.builder()
            .scheduleLayoutDtos(scheduleLayoutDtos)
            .scheduleLayoutNoLocationDto(scheduleLayoutNoLocationDto)
            .scheduleStatistics(stats)
            .build();
    }

    private ScheduleLayoutDto buildScheduleLayoutDto(Location location, List<Shift> shifts) {
        // sort for deterministic column placement
        int requiredShiftColumns = getRequiredShiftColumns(shifts);
        return ScheduleLayoutDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .requiredShiftColumns(requiredShiftColumns)
            .build();
    }

    private ScheduleLayoutNoLocationDto buildScheduleLayoutNoLocationDto(List<Shift> shifts) {
        int requiredShiftColumns = getRequiredShiftColumns(shifts);
        return ScheduleLayoutNoLocationDto.builder()
            .requiredShiftColumns(requiredShiftColumns)
            .build();
    }

    private int getRequiredShiftColumns(List<Shift> shifts) {
        // sort for deterministic column placement
        var sorted = shifts.stream()
            .sorted(Comparator
                .comparing(Shift::getStartTime)
                .thenComparing(Shift::getEndTime)).toList();
        var shiftColumns = calculateShiftColumns(sorted);
        return shiftColumns.stream()
            .mapToInt(ShiftColumnDto::getColumnIndex)
            .max()
            .orElse(-1) + 1;
    }

    @Override
    public EventScheduleContentDto getEventScheduleContent(long eventId, EventScheduleDaySearchDto searchDto) {
        var shiftsByLocation = getScheduleShiftsByLocation(shiftPlanId, searchDto);
        // Build location DTOs
        var scheduleContentDtos = shiftsByLocation.entrySet().stream()
            .map(entry -> buildScheduleContentDto(entry.getKey(), entry.getValue()))
            .toList();

        var scheduleContentNoLocationDto = buildScheduleContentNoLocationDto(shiftPlanId, searchDto);

        return EventScheduleContentDto.builder()
            .date(searchDto != null ? searchDto.getDate() : null)
            .scheduleContentDtos(scheduleContentDtos)
            .scheduleContentNoLocationDto(scheduleContentNoLocationDto)
            .build();
    }

    private Map<Location, List<Shift>> getScheduleShiftsByLocation(long shiftPlanId, EventScheduleFilterDto filterDto) {
        var userId = validateShiftPlanAccessAndGetUserId(shiftPlanId);
        // if param is ShiftPlanScheduleFilterDto filtering is done without date; date filtering is only done if param is ShiftPlanScheduleDaySearchDto instance
        var filteredShiftsWithoutViewMode = shiftDao.searchShiftsInShiftPlan(shiftPlanId, userId, filterDto);
        var queriedShifts = getShiftsBasedOnViewModes(shiftPlanId, userId, filterDto, filteredShiftsWithoutViewMode);
        Map<Location, List<Shift>> shiftsByLocation = new HashMap<>();
        for (var shift : queriedShifts) {
            if (shift.getLocation() == null) {
                continue;
            }
            shiftsByLocation.computeIfAbsent(shift.getLocation(), k -> new ArrayList<>()).add(shift);
        }
        // add all other locations of the event without shifts
        var locations = shiftPlanDao.getById(shiftPlanId).getEvent().getLocations();
        for (var location : locations) {
            shiftsByLocation.putIfAbsent(location, new ArrayList<>());
        }

        return shiftsByLocation;
    }

    private String validateShiftPlanAccessAndGetUserId(long shiftPlanId) {
        securityHelper.assertUserIsInPlan(shiftPlanId);
        return userProvider.getCurrentUser().getUserId();
    }

    private boolean isSignupPossible(PositionSlot slot, Volunteer volunteer) {
        var state = eligibilityService.getSignupStateForPositionSlot(slot, volunteer);
        // no further actions needed if not eligible
        if (state == PositionSignupState.NOT_ELIGIBLE) {
            return false;
        }
        boolean freeAndOpenTrade = state == PositionSignupState.SIGNUP_OR_TRADE;
        boolean freeAndEligible = state == PositionSignupState.SIGNUP_POSSIBLE;
        boolean hasOpenTrade = state == PositionSignupState.SIGNUP_VIA_TRADE;
        boolean hasAuction = state == PositionSignupState.SIGNUP_VIA_AUCTION;
        return freeAndEligible || freeAndOpenTrade || hasOpenTrade || hasAuction;
    }

    private ScheduleContentDto buildScheduleContentDto(Location location, List<Shift> shifts) {
        // sort for deterministic column placement
        shifts.sort(Comparator
            .comparing(Shift::getStartTime)
            .thenComparing(Shift::getEndTime));
        var shiftColumns = calculateShiftColumns(shifts);
        // get activities related to this location (shift unrelated, even when no shifts are present)
        var activitiesRelatedToLocation = activityDao.findAllByLocationId(location.getId()).stream()
            .distinct()
            .map(ActivityMapper::toActivityDto)
            .toList();
        return ScheduleContentDto.builder()
            .location(LocationMapper.toLocationDto(location))
            .activities(activitiesRelatedToLocation)
            .shiftColumns(shiftColumns)
            .build();
    }

    private ScheduleContentNoLocationDto buildScheduleContentNoLocationDto(
        long shiftPlanId,
        EventScheduleDaySearchDto searchDto) {

        // get activities without location
        var activitiesWithoutLocation = activityDao.findAllWithoutLocationByShiftPlanId(shiftPlanId).stream()
            .distinct()
            .map(ActivityMapper::toActivityDto)
            .toList();

        // get shifts without location
        var shiftsWithoutLocation = shiftDao.searchShiftsInShiftPlan(shiftPlanId,
                userProvider.getCurrentUser().getUserId(),
                searchDto).stream()
            .filter(shift -> shift.getLocation() == null)
            .toList();

        var filteredShiftsWithoutLocation = getShiftsBasedOnViewModes(shiftPlanId,
            userProvider.getCurrentUser().getUserId(),
            searchDto,
            shiftsWithoutLocation);

        var shiftColumns = calculateShiftColumns(filteredShiftsWithoutLocation);

        return ScheduleContentNoLocationDto.builder()
            .activities(activitiesWithoutLocation)
            .shiftColumns(shiftColumns)
            .build();
    }

    private List<Shift> getShiftsBasedOnViewModes(
        long shiftPlanId,
        String userId,
        EventScheduleFilterDto filterDto,
        List<Shift> filteredShiftsWithoutViewMode) {
        if (filterDto != null
            && filterDto.getShiftRelevances() != null
            && !filterDto.getShiftRelevances().isEmpty()) {
            List<Shift> ownShifts = new ArrayList<>();
            List<Shift> signUpPossibleShifts = new ArrayList<>();
            if (filterDto.getShiftRelevances().contains(ShiftRelevance.MY_SHIFTS)) {
                ownShifts = shiftDao.searchUserRelatedShiftsInShiftPlan(shiftPlanId, userId);
            }
            if (filterDto.getShiftRelevances().contains(ShiftRelevance.SIGNUP_POSSIBLE)) {
                signUpPossibleShifts = new ArrayList<>(filteredShiftsWithoutViewMode);
                var volunteer = volunteerDao.getById(userId);
                signUpPossibleShifts = signUpPossibleShifts.stream()
                    .filter(shift -> shift.getSlots().stream().anyMatch(slot ->
                        isSignupPossible(slot, volunteer)
                    ))
                    .toList();
            }
            // combine results
            var combinedShifts = new ArrayList<>(ownShifts);
            for (var shift : signUpPossibleShifts) {
                if (!combinedShifts.contains(shift)) {
                    combinedShifts.add(shift);
                }
            }
            // combine combined shifts with filteredShiftsWithoutViewMode to apply other filters
            return combinedShifts.stream()
                .filter(filteredShiftsWithoutViewMode::contains)
                .toList();
        }
        return filteredShiftsWithoutViewMode;
    }

    private List<ShiftColumnDto> calculateShiftColumns(List<Shift> sortedShifts) {
        // end time per column
        var columnEndTimes = new ArrayList<Instant>();
        var result = new ArrayList<ShiftColumnDto>(sortedShifts.size());
        for (var shift : sortedShifts) {
            int columnIndex = findFirstFreeColumnIndex(columnEndTimes, shift.getStartTime());
            if (columnIndex == -1) {
                columnIndex = columnEndTimes.size();
                columnEndTimes.add(shift.getEndTime());
            } else {
                columnEndTimes.set(columnIndex, shift.getEndTime());
            }
            result.add(ShiftColumnDto.builder()
                .columnIndex(columnIndex)
                .shiftDto(shiftMapper.assemble(shift))
                .build());
        }
        return result;
    }

    private int findFirstFreeColumnIndex(List<Instant> columnEndTimes, Instant startTime) {
        for (int i = 0; i < columnEndTimes.size(); i++) {
            // column is free if previous shift ended at or before this start
            if (!columnEndTimes.get(i).isAfter(startTime)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public EventScheduleFilterValuesDto getEventScheduleFilterValues(long eventId) {
        var event = eventDao.getById(eventId);
        var shiftPlans = event.getShiftPlans();
        if (shiftPlans == null || shiftPlans.isEmpty()) {
            return EventScheduleFilterValuesDto.builder()
                .locations(List.of())
                .roles(List.of())
                .firstDate(TimeUtil.convertToUtcLocalDate(event.getStartTime()))
                .lastDate(TimeUtil.convertToUtcLocalDate(event.getEndTime()))
                .build();
        }

        var allShifts = shiftPlans.stream()
            .flatMap(shiftPlan -> shiftPlan.getShifts().stream())
            .filter(Objects::nonNull)
            .toList();

        return getFilterValuesFromAllShifts(allShifts, event);
    }

    private EventScheduleFilterValuesDto getFilterValuesFromAllShifts(Collection<Shift> shifts, Event event) {
        if (shifts.isEmpty()) {
            return EventScheduleFilterValuesDto.builder()
                .locations(List.of())
                .roles(List.of())
                .firstDate(TimeUtil.convertToUtcLocalDate(event.getStartTime()))
                .lastDate(TimeUtil.convertToUtcLocalDate(event.getEndTime()))
                .build();
        }
        var locations = shifts.stream()
            .map(Shift::getLocation)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        var roles = shifts.stream()
            .flatMap(shift -> shift.getSlots().stream())
            .map(PositionSlot::getRole)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        // Determine first and last date from shifts and related activities
        var firstDate = Stream.concat(
                shifts.stream().map(Shift::getStartTime),
                shifts.stream()
                    .map(Shift::getRelatedActivity)
                    .filter(Objects::nonNull)
                    .map(Activity::getStartTime)
            )
            .filter(Objects::nonNull)
            .min(Instant::compareTo)
            .map(TimeUtil::convertToUtcLocalDate)
            .orElse(TimeUtil.convertToUtcLocalDate(event.getStartTime()));
        var lastDate = Stream.concat(
                shifts.stream().map(Shift::getEndTime),
                shifts.stream()
                    .map(Shift::getRelatedActivity)
                    .filter(Objects::nonNull)
                    .map(Activity::getEndTime)
            )
            .filter(Objects::nonNull)
            .max(Instant::compareTo)
            .map(TimeUtil::convertToUtcLocalDate)
            .orElse(TimeUtil.convertToUtcLocalDate(event.getEndTime()));

        return EventScheduleFilterValuesDto.builder()
            .locations(locations.isEmpty() ? List.of() : LocationMapper.toLocationDto(locations))
            .roles(roles.isEmpty() ? List.of() : RoleMapper.toRoleDto(roles))
            .firstDate(firstDate)
            .lastDate(lastDate)
            .build();
    }
}
