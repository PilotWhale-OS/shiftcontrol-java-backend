package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

public interface TimeConstraintDao extends BasicDao<TimeConstraint, Long> {
    Collection<TimeConstraint> searchByVolunteerAndEvent(String volunteerId, long eventId);

    Collection<TimeConstraint> searchByVolunteerAndEventAndType(String volunteerId, long eventId, TimeConstraintType type);
}
