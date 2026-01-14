package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;

public interface ActivityDao extends BasicDao<Activity, Long> {
    Collection<Activity> findAllByLocationId(Long locationId);

    Collection<Activity> findAllByEventId(Long eventId);

    Collection<Activity> findAllWithoutLocationByShiftPlanId(Long shiftPlanId);

    Collection<Activity> searchActivitiesInEvent(Long eventId, EventScheduleDaySearchDto searchDto);

    Optional<Activity> findByEventAndName(Long eventId, String name);
}
