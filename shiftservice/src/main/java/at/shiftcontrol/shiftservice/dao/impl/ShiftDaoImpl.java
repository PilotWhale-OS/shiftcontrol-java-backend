package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.impl.specification.ShiftSpecifications;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import at.shiftcontrol.shiftservice.type.ScheduleViewType;

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
    public Collection<Shift> saveAll(Collection<Shift> entities) {
        return shiftRepository.saveAll(entities);
    }

    @Override
    public void delete(Shift entity) {
        shiftRepository.delete(entity);
    }


    @Override
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, String userId) {
        var spec = ShiftSpecifications.inShiftPlan(shiftPlanId)
            .and(ShiftSpecifications.assignedToUser(userId));

        return shiftRepository.findAll(spec);
    }

    @Override
    public List<Shift> searchShiftsInShiftPlan(long shiftPlanId, String userId, ShiftPlanScheduleFilterDto filterDto) {
        Specification<Shift> spec =
            ShiftSpecifications.inShiftPlan(shiftPlanId)
                .and(ShiftSpecifications.matchesSearchDto(filterDto)); // your other filters

        if (filterDto != null && filterDto.getScheduleViewType() == ScheduleViewType.MY_SHIFTS) {
            spec = spec.and(ShiftSpecifications.assignedToUser(userId));
        }

        return shiftRepository.findAll(spec);
    }
}
