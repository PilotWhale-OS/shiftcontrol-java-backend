package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

public interface AssignmentDao extends BasicDao<Assignment, AssignmentId> {
    Collection<Assignment> findAuctionsByShiftPlanId(long shiftPlanId);
}
