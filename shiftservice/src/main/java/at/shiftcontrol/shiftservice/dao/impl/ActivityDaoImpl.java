package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;

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
    public void delete(Activity entity) {
        activityRepository.delete(entity);
    }
}
