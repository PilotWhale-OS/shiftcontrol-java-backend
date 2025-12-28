package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ActivityDaoImpl implements ActivityDao {
    private final ActivityRepository activityRepository;

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
}
