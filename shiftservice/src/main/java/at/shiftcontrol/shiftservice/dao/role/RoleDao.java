package at.shiftcontrol.shiftservice.dao.role;

import java.util.List;

import at.shiftcontrol.shiftservice.dao.BasicDao;
import at.shiftcontrol.shiftservice.entity.role.Role;

public interface RoleDao extends BasicDao<Role, Long> {
    List<Role> findAllByShiftPlanId(Long eventId);
}
