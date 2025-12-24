package at.shiftcontrol.shiftservice.dao;

import java.util.Optional;

import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;

public interface ShiftPlanInviteDao extends BasicDao<ShiftPlanInvite, Long> {
    Optional<ShiftPlanInvite> findByCode(String code);

    boolean existsByCode(String code);
}
