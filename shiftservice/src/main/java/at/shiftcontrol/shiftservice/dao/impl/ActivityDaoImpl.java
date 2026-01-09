package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.impl.specification.ActivitySpecifications;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;

@RequiredArgsConstructor
@Component
public class ActivityDaoImpl implements ActivityDao {
    private final ActivityRepository activityRepository;

    @Override
    public String getName() {
        return "Activity";
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return activityRepository.findById(id);
    }


    @Override
    public Activity save(Activity entity) {
        return activityRepository.save(entity);
    }

    @Override
    public Collection<Activity> saveAll(Collection<Activity> entities) {
        return activityRepository.saveAll(entities);
    }

    @Override
    public void delete(Activity entity) {
        activityRepository.delete(entity);
    }

    @Override
    public Collection<Activity> findAllByLocationId(Long locationId) {
        return activityRepository.findAllByLocationId(locationId);
    }

    @Override
    public Collection<Activity> findAllByEventId(Long eventId) {
        return activityRepository.findAllByEventId(eventId);
    }

    @Override
    public Collection<Activity> findAllWithoutLocationByShiftPlanId(Long shiftPlanId) {
        return activityRepository.findAllWithoutLocationByShiftPlanId(shiftPlanId);
    }

    @Override
    public Collection<Activity> searchActivitiesInEvent(Long eventId, EventScheduleDaySearchDto searchDto) {
        Specification<Activity> spec = ActivitySpecifications.matchesSearchDto(searchDto);

        return activityRepository.findAll(spec);
    }
}
