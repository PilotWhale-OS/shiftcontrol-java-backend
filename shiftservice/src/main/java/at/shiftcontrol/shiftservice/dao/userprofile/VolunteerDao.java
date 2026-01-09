package at.shiftcontrol.shiftservice.dao.userprofile;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dao.BasicDao;
import at.shiftcontrol.shiftservice.entity.Volunteer;

public interface VolunteerDao extends BasicDao<Volunteer, String> {
    Collection<Volunteer> findAllByShiftPlan(long shiftPlanId);

    Collection<Volunteer> findAllByShiftPlanAndVolunteerIds(long shiftPlanId, Collection<String> volunteerIds);

    Collection<Volunteer> findAllByEvent(long eventId);
}
