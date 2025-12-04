package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;

@RequiredArgsConstructor
@Component
public class ShiftPlanDaoImpl implements ShiftPlanDao {
    private final ShiftPlanRepository shiftPlanRepository;

    @Override
    public Optional<ShiftPlan> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public ShiftPlan save(ShiftPlan entity) {
        return null;
    }

    @Override
    public void delete(ShiftPlan entity) {
    }
}
