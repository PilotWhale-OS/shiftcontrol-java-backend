package at.shiftcontrol.shiftservice.dao.role;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.shiftservice.dao.BasicDao;

public interface RoleDao extends BasicDao<Role, Long> {
    List<Role> findAllByShiftPlanId(Long eventId);

    Collection<Role> findAllById(Collection<Long> roleIds);
}
