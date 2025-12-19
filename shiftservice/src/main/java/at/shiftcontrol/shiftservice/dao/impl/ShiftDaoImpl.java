package at.shiftcontrol.shiftservice.dao.impl;

import java.util.List;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftDaoImpl implements ShiftDao {
    private final ShiftRepository shiftRepository;

    @Override
    public Optional<Shift> findById(Long id) {
        return shiftRepository.findById(id);
    }

    @Override
    public Shift save(Shift entity) {
        return shiftRepository.save(entity);
    }

    @Override
    public void delete(Shift entity) {
        shiftRepository.delete(entity);
    }


    @Override
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, long userId) {
        var spec = ShiftSpecifications.inShiftPlan(shiftPlanId)
            .and(ShiftSpecifications.assignedToUser(userId));
 
        return shiftRepository.findAll(spec);
    }

    @Override
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, long userId, ShiftPlanScheduleSearchDto searchDto) {
        var spec = ShiftSpecifications.inShiftPlan(shiftPlanId)
            .and(ShiftSpecifications.assignedToUser(userId))
            .and(ShiftSpecifications.matchesSearchDto(searchDto));

        return shiftRepository.findAll(spec);
    }
}
