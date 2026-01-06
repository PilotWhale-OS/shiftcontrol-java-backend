package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;

@RequiredArgsConstructor
@Component
public class ShiftPlanDaoImpl implements ShiftPlanDao {
    private final ShiftPlanRepository shiftPlanRepository;

    @Override
    public String getName() {
        return "ShiftPlan";
    }

    @Override
    public Optional<ShiftPlan> findById(Long id) {
        return shiftPlanRepository.findById(id);
    }

    @Override
    public ShiftPlan save(ShiftPlan entity) {
        return shiftPlanRepository.save(entity);
    }

    @Override
    public Collection<ShiftPlan> saveAll(Collection<ShiftPlan> entities) {
        return shiftPlanRepository.saveAll(entities);
    }

    @Override
    public void delete(ShiftPlan entity) {
        shiftPlanRepository.delete(entity);
    }

    @Override
    public Collection<ShiftPlan> findByEventId(Long eventId) {
        return shiftPlanRepository.findByEventId(eventId);
    }

    @Override
    public Collection<ShiftPlan> findAllUserRelatedShiftPlans(String userId) {
        return shiftPlanRepository.findAllUserRelatedShiftPlans(userId);
    }
}
