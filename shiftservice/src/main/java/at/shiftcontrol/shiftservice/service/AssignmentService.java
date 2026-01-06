package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.Volunteer;

public interface AssignmentService {
    /**
     * reassigns the asssignment to the given volunteer.
     * handles all dependencies of the assignment, since reassigning results in a new primary key
     * deletes the old assignment and persists a new one in the process
     *
     * @param oldAssignment the assignment containing the old assigned volunteer
     * @param newVolunteer volunteer to replace the old volunteer
     * @return new assignment where the given volunteer is assigned
     */
    Assignment reassign(Assignment oldAssignment, Volunteer newVolunteer);
}
