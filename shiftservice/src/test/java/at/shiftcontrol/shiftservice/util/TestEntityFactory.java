package at.shiftcontrol.shiftservice.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.PositionConstraint;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Role;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;
import at.shiftcontrol.shiftservice.repo.AttendanceRepository;
import at.shiftcontrol.shiftservice.repo.AttendanceTimeConstraintRepository;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import at.shiftcontrol.shiftservice.repo.LocationRepository;
import at.shiftcontrol.shiftservice.repo.PositionConstraintRepository;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;
import at.shiftcontrol.shiftservice.repo.RoleRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(2))
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
            .startTime(Instant.now())
            .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
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
