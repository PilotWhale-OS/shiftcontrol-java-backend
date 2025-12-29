package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

public interface AttendanceTimeConstraintDao extends BasicDao<AttendanceTimeConstraint, Long> {
    Collection<AttendanceTimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId);

    Collection<AttendanceTimeConstraint> searchByVolunteerAndEventAndType(String volunteerId, long eventId, TimeConstraintType type);
}
