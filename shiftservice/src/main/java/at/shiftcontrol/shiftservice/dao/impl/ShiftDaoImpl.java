package at.shiftcontrol.shiftservice.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dao.impl.specification.ShiftSpecifications;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;

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
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, String userId) {
        var spec = ShiftSpecifications.inShiftPlan(shiftPlanId)
            .and(ShiftSpecifications.assignedToUser(userId));

        return shiftRepository.findAll(spec);
    }

    @Override
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, String userId, ShiftPlanScheduleSearchDto searchDto) {
        Specification<Shift> spec =
            ShiftSpecifications.inShiftPlan(shiftPlanId)
                .and(ShiftSpecifications.matchesSearchDto(searchDto)); // your other filters

        if (searchDto != null && searchDto.getScheduleViewType() != null) {
            spec = switch (searchDto.getScheduleViewType()) {
                case MY_SHIFTS -> spec.and(ShiftSpecifications.assignedToUser(userId));
                case SIGNUP_POSSIBLE -> spec.and(ShiftSpecifications.signupPossibleForUser(userId));
            };
        }

        return shiftRepository.findAll(spec);
    }
}
