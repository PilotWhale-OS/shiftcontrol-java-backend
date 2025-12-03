package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;

@RequiredArgsConstructor
@Component
public class ActivityDaoImpl implements ActivityDao {
    private final ActivityRepository activityRepository;
}
