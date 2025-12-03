package at.shiftcontrol.shiftservice.dao.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;

@RequiredArgsConstructor
@Component
public class ShiftPlanDaoImpl implements ShiftPlanDao {
    private final ShiftPlanRepository shiftPlanRepository;
}
