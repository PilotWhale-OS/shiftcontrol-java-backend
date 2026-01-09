package at.shiftcontrol.shiftservice.dao.userprofile;

import java.util.Collection;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dao.BasicDao;

public interface VolunteerDao extends BasicDao<Volunteer, String> {
    Collection<Volunteer> findAllByShiftPlan(long shiftPlanId);

    Collection<Volunteer> findAllByShiftPlanAndVolunteerIds(long shiftPlanId, Collection<String> volunteerIds);
}
