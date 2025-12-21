package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import at.shiftcontrol.shiftservice.repo.ShiftPlanInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftPlanInviteDaoImpl implements ShiftPlanInviteDao {
    private final ShiftPlanInviteRepository shiftPlanInviteRepository;

    @Override
    public Optional<ShiftPlanInvite> findById(Long id) {
        return shiftPlanInviteRepository.findById(id);
    }

    @Override
    public ShiftPlanInvite save(ShiftPlanInvite entity) {
        return shiftPlanInviteRepository.save(entity);
    }

    @Override
    public void delete(ShiftPlanInvite entity) {
        shiftPlanInviteRepository.delete(entity);
    }


    @Override
    public Optional<ShiftPlanInvite> findByCode(String code) {
        return shiftPlanInviteRepository.findByCode(code);
    }
}
