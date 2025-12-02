package at.shiftcontrol.shiftservice.util;

import at.shiftcontrol.shiftservice.entity.*;
import at.shiftcontrol.shiftservice.repo.*;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TestEntityFactory {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShiftPlanRepository shiftPlanRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceTimeConstraintRepository attendanceTimeConstraintRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PositionSlotRepository positionSlotRepository;

    @Autowired
    private PositionConstraintRepository positionConstraintRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    public Event createPersistedEvent() {
        Event event = Event.builder()
                .name("eventName")
                .startTime(Instant.now())
                .endTime(Instant.now())
                .build();
        // TODO add other fields
        return eventRepository.save(event);
    }

    public ShiftPlan createPersistedShiftPlan() {
        throw new NotImplementedException();
    }

    public Shift createPersistedShift() {
        throw new NotImplementedException();
    }

    public Activity createPersistedActivity() {
        Activity activity = Activity.builder()
                .name("activityName")
                .description("activityDescription")
                .event(createPersistedEvent())
                .build();
        return activityRepository.save(activity);
    }

    public Location createPersistedLocation() {
        throw new NotImplementedException();
    }

    public Attendance createPersistedAttendance() {
        throw new NotImplementedException();
    }

    public AttendanceTimeConstraint createPersistedAttendanceTimeConstraint() {
        throw new NotImplementedException();
    }

    public Volunteer createPersistedVolunteer() {
        throw new NotImplementedException();
    }

    public Role createPersistedRole() {
        Role role = Role.builder()
                .name("testRole")
                .description("This is a role description.")
                .build();
        return roleRepository.save(role);
    }

    public PositionSlot createPersistedPositionSlot() {
        throw new NotImplementedException();
    }

    public PositionConstraint createPersistedPositionConstraint() {
        throw new NotImplementedException();
    }

    public Assignment createPersistedAssignment() {
        throw new NotImplementedException();
    }

    public AssignmentSwitchRequest createPersistedAssignmentSwitchRequest() {
        throw new NotImplementedException();
    }

}
