package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.ShiftPlanInviteDao;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import at.shiftcontrol.shiftservice.repo.ShiftPlanInviteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftPlanInviteDaoImpl implements ShiftPlanInviteDao {
    private final ShiftPlanInviteRepository shiftPlanInviteRepository;

    @Override
    public String getName() {
        return "ShiftPlanInvite";
    }

    @Override
    public Optional<ShiftPlanInvite> findById(Long id) {
        return shiftPlanInviteRepository.findById(id);
    }

    @Override
    public ShiftPlanInvite save(ShiftPlanInvite entity) {
        return shiftPlanInviteRepository.save(entity);
    }

    @Override
    public Collection<ShiftPlanInvite> saveAll(Collection<ShiftPlanInvite> entities) {
        return shiftPlanInviteRepository.saveAll(entities);
    }

    @Override
    public void delete(ShiftPlanInvite entity) {
        shiftPlanInviteRepository.delete(entity);
    }

    @Override
    public ShiftPlanInvite getByCode(String code) {
        LoggerFactory.getLogger("ShiftPlanInviteDaoLogger").info("Invite code not found: {}", code);
        return shiftPlanInviteRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Invite code not found"));
    }

    @Override
    public boolean existsByCode(String code) {
        return shiftPlanInviteRepository.existsByCode(code);
    }

    @Override
    public Collection<ShiftPlanInvite> findAllByShiftPlanId(Long shiftPlanId) {
        return shiftPlanInviteRepository.findAll().stream()
            .filter(invite -> invite.getShiftPlan().getId() == shiftPlanId)
            .toList();
    }
}
