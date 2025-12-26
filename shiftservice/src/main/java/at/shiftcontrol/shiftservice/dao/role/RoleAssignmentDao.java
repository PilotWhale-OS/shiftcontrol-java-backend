package at.shiftcontrol.shiftservice.dao.role;

import java.util.List;

import at.shiftcontrol.shiftservice.dao.BasicDao;
import at.shiftcontrol.shiftservice.entity.role.RoleAssignment;

public interface RoleAssignmentDao extends BasicDao<RoleAssignment, Long> {
    List<RoleAssignment> findAllByRole_ShiftPlanIdAndUserId(Long shiftPlanId, String userId);
}
