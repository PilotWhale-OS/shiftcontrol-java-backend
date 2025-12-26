package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

public interface AttendanceTimeConstraintDao extends BasicDao<AttendanceTimeConstraint, Long> {
    Collection<AttendanceTimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId);
}
