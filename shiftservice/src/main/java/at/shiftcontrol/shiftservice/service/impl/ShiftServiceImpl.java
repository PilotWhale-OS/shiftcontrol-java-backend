package at.shiftcontrol.shiftservice.service.impl;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.ShiftEvent;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.service.ShiftService;
import at.shiftcontrol.shiftservice.service.UserPreferenceService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftDao shiftDao;
    private final ShiftPlanDao shiftPlanDao;
    private final ActivityDao activityDao;
    private final LocationDao locationDao;
    private final UserPreferenceService userPreferenceService;
    private final ShiftAssemblingMapper shiftAssemblingMapper;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public ShiftDetailsDto getShiftDetails(long shiftId, String userId) throws NotFoundException {
        var shift = shiftDao.findById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found"));
        var userPref = userPreferenceService.getUserPreference(userId, shiftId);

        return ShiftDetailsDto.builder()
            .shift(shiftAssemblingMapper.assemble(shift))
            .preference(userPref)
            .build();
    }

    @Override
    public ShiftDto createShift(long shiftPlanId, @NonNull ShiftModificationDto modificationDto) throws NotFoundException, ForbiddenException {
        securityHelper.assertUserIsPlanner(shiftPlanId);

        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(() -> new NotFoundException("Shift plan not found"));
        var newShift = Shift.builder()
            .shiftPlan(shiftPlan)
            .build();

        validateModificationDtoAndSetShiftFields(modificationDto, newShift);
        newShift = shiftDao.save(newShift);

        publisher.publishEvent(ShiftEvent.of(newShift, RoutingKeys.SHIFT_CREATED));
        return shiftAssemblingMapper.assemble(newShift);
    }

    @Override
    public ShiftDto updateShift(long shiftId, @NonNull ShiftModificationDto modificationDto) throws NotFoundException, ForbiddenException {
        var shift = shiftDao.findById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found"));
        securityHelper.assertUserIsPlanner(shift);

        validateModificationDtoAndSetShiftFields(modificationDto, shift);

        shift = shiftDao.save(shift);

        publisher.publishEvent(ShiftEvent.of(shift, RoutingKeys.formatStrict(RoutingKeys.SHIFT_UPDATED, Map.of("shiftId", String.valueOf(shiftId)))));
        return shiftAssemblingMapper.assemble(shift);
    }

    private void validateModificationDtoAndSetShiftFields(ShiftModificationDto modificationDto, Shift shift) throws NotFoundException {
        if (StringUtils.isNotBlank(modificationDto.getActivityId()) && StringUtils.isNotBlank(modificationDto.getLocationId())) {
            throw new BadRequestException("Cannot set both related activity and location");
        }

        if (StringUtils.isBlank(modificationDto.getActivityId()) && StringUtils.isBlank(modificationDto.getLocationId())) {
            throw new BadRequestException("Either related activity or location must be set");
        }

        var event = shift.getShiftPlan().getEvent();

        if (modificationDto.getStartTime().isAfter(modificationDto.getEndTime())) {
            throw new BadRequestException("Shift start time must be before end time");
        }

        if (modificationDto.getStartTime().isBefore(event.getStartTime()) ||
            modificationDto.getEndTime().isAfter(event.getEndTime()) ||
            modificationDto.getEndTime().isBefore(modificationDto.getStartTime())) {
            throw new BadRequestException("Shift time must be within event time range");
        }

        shift.setName(modificationDto.getName());
        shift.setShortDescription(modificationDto.getShortDescription());
        shift.setLongDescription(modificationDto.getLongDescription());
        shift.setStartTime(modificationDto.getStartTime());
        shift.setEndTime(modificationDto.getEndTime());
        if (StringUtils.isNotBlank(modificationDto.getActivityId())) {
            var activity = activityDao.findById(ConvertUtil.idToLong(modificationDto.getActivityId()))
                .orElseThrow(() -> new NotFoundException("Activity not found"));
            shift.setRelatedActivity(activity);

            if (activity.getLocation() != null) {
                var eventLocations = shift.getShiftPlan().getEvent().getLocations();
                if (!eventLocations.contains(activity.getLocation())) {
                    throw new BadRequestException("Activity location does not belong to the same event as the shift");
                }
                shift.setLocation(activity.getLocation());
            }
        } else {
            var location = locationDao.findById(ConvertUtil.idToLong(modificationDto.getLocationId()))
                .orElseThrow(() -> new NotFoundException("Location not found"));

            var eventLocations = shift.getShiftPlan().getEvent().getLocations();
            if (!eventLocations.contains(location)) {
                throw new BadRequestException("Location does not belong to the same event as the shift");
            }
            shift.setLocation(location);
            shift.setRelatedActivity(null);
        }
    }

    @Override
    public void deleteShift(long shiftId) throws NotFoundException, ForbiddenException {
        var shift = shiftDao.findById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found"));
        securityHelper.assertUserIsPlanner(shift);

        shiftDao.delete(shift);
        publisher.publishEvent(ShiftEvent.of(shift, RoutingKeys.formatStrict(RoutingKeys.SHIFT_DELETED, Map.of("shiftId", String.valueOf(shiftId)))));
    }
}
