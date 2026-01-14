package at.shiftcontrol.shiftservice.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.entity.PositionConstraint;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.TimeConstraint;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.repo.ActivityRepository;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.repo.AssignmentSwitchRequestRepository;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import at.shiftcontrol.shiftservice.repo.LocationRepository;
import at.shiftcontrol.shiftservice.repo.PositionConstraintRepository;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import at.shiftcontrol.shiftservice.repo.TimeConstraintRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsCalculator;

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
    private TimeConstraintRepository timeConstraintRepository;
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

    @Autowired
    private RewardPointsCalculator rewardPointsCalculator;

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
            .startTime(Instant.now())
            .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
            .build();
        return activityRepository.save(activity);
    }

    public Location createPersistedLocation() {
        throw new NotImplementedException();
    }

    public TimeConstraint createPersistedAttendanceTimeConstraint() {
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

    public UserProfileDto getUserProfileDtoWithId(String userId) {
        UserProfileDto profile = new UserProfileDto();
        VolunteerDto volunteer = new VolunteerDto(
            userId,
            "first name",
            "last name"
        );
        AccountInfoDto info = new AccountInfoDto(
            volunteer,
            "Test Username",
            "mail@mail.com",
            UserType.ASSIGNED
        );
        profile.setAccount(info);
        return profile;
    }

    public UserRepresentation getUserRepresentationWithId(String id) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId(id);
        userRep.setFirstName("Kerbert");
        userRep.setLastName("Huttelwascher");
        return userRep;
    }

    public PositionSlotRequestDto getPositionSlotRequestDto(long positionSlotId) {
        PositionSlot positionSlot = positionSlotRepository.getReferenceById(positionSlotId);
        String hash = rewardPointsCalculator.calculatePointsConfigHash(positionSlot);
        return new PositionSlotRequestDto(hash);
    }
}
