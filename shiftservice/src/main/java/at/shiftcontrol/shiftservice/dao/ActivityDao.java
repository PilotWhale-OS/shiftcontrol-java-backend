package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.Activity;

public interface ActivityDao extends BasicDao<Activity, Long> {
    Collection<Activity> getRelatedActivities(long shiftId);
}
