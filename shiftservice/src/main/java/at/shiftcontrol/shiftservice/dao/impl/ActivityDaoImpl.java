package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.Activity;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;

@RequiredArgsConstructor
@Component
public class ActivityDaoImpl implements ActivityDao {
    private final ActivityRepository activityRepository;

    @Override
    public Optional<Activity> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Activity save(Activity entity) {
        return null;
    }

    @Override
    public void delete(Activity entity) {
    }
}
