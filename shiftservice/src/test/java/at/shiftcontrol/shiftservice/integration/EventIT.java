package at.shiftcontrol.shiftservice.integration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Set;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.integration.config.RestITBase;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;
import io.restassured.http.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class EventIT extends RestITBase {
    private static final String EVENT_CLONE_PATH = "/events/%d/clone";

    @Autowired
    PositionSlotRepository positionSlotRepository;

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    ShiftPlanRepository shiftPlanRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    VolunteerRepository volunteerRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    private Event eventA;
    private ShiftPlan shiftPlanA;
    private Shift shiftA, shiftB;

    private Volunteer volunteerA, volunteerB;

    private PositionSlot positionSlotA, positionSlotB;

    private Assignment assignmentA;

    @BeforeEach
    void setUp() {
        positionSlotRepository.deleteAll();
        shiftRepository.deleteAll();
        shiftPlanRepository.deleteAll();
        eventRepository.deleteAll();
        volunteerRepository.deleteAll();
        assignmentRepository.deleteAll();

        createEvents();
        createShiftPlans();
        createShifts();
        createPositionSlots();
        createVolunteers();
        createAssignments();
    }

    private void createEvents() {
        var startTimeA = LocalDateTime.of(2024, 2, 1, 7, 0).toInstant(ZoneOffset.UTC);
        var endTimeA = LocalDateTime.of(2024, 2, 3, 22, 0).toInstant(ZoneOffset.UTC);

        eventA = Event.builder()
            .name("EventA")
            .startTime(startTimeA)
            .endTime(endTimeA)
            .build();

        eventRepository.save(eventA);

        assertAll(
            () -> assertThat(eventA.getId()).isGreaterThan(0),
            () -> assertThat(eventRepository.existsById(eventA.getId())).isTrue()
        );
    }

    private void createShiftPlans() {
        shiftPlanA = ShiftPlan.builder()
            .name("ShiftPlanA")
            .event(eventA)
            .lockStatus(LockStatus.SUPERVISED)
            .defaultNoRolePointsPerMinute(1)
            .build();

        shiftPlanRepository.save(shiftPlanA);

        assertAll(
            () -> assertThat(shiftPlanA.getId()).isGreaterThan(0),
            () -> assertThat(shiftPlanRepository.existsById(shiftPlanA.getId())).isTrue()
        );
    }

    private void createShifts() {
        var shifts = new ArrayList<Shift>();

        var startTimeA = LocalDateTime.of(2024, 1, 1, 10, 0).toInstant(ZoneOffset.UTC);
        var endTimeA = LocalDateTime.of(2024, 1, 1, 20, 0).toInstant(ZoneOffset.UTC);

        shiftA = Shift.builder()
            .name("ShiftA")
            .shiftPlan(shiftPlanA)
            .shortDescription("ShortDescA")
            .longDescription("This is a description for ShiftA")
            .startTime(startTimeA)
            .endTime(endTimeA)
            .bonusRewardPoints(0)
            .build();
        shifts.add(shiftA);

        var startTimeB = LocalDateTime.of(2024, 2, 1, 11, 0).toInstant(ZoneOffset.UTC);
        var endTimeB = LocalDateTime.of(2024, 2, 3, 18, 0).toInstant(ZoneOffset.UTC);

        shiftB = Shift.builder()
            .name("ShiftB")
            .shiftPlan(shiftPlanA)
            .shortDescription("ShortDescB")
            .longDescription("This is a description for ShiftB")
            .startTime(startTimeB)
            .endTime(endTimeB)
            .bonusRewardPoints(10)
            .build();
        shifts.add(shiftB);

        shiftRepository.saveAll(shifts);

        assertAll(
            () -> assertThat(shiftA.getId()).isGreaterThan(0),
            () -> assertThat(shiftB.getId()).isGreaterThan(0),
            () -> assertThat(shiftRepository.existsById(shiftA.getId())).isTrue(),
            () -> assertThat(shiftRepository.existsById(shiftB.getId())).isTrue()
        );
    }

    private void createPositionSlots() {
        var slots = new ArrayList<PositionSlot>();

        positionSlotA = PositionSlot.builder()
            .name("PositionSlotA")
            .skipAutoAssignment(false)
            .shift(shiftA)
            .description("DescriptionA")
            .desiredVolunteerCount(3)
            .build();
        slots.add(positionSlotA);

        positionSlotB = PositionSlot.builder()
            .name("PositionSlotB")
            .skipAutoAssignment(true)
            .shift(shiftB)
            .description("DescriptionB")
            .desiredVolunteerCount(5)
            .overrideRewardPoints(70)
            .build();
        slots.add(positionSlotB);

        positionSlotRepository.saveAll(slots);

        assertAll(
            () -> assertThat(positionSlotA.getId()).isGreaterThan(0),
            () -> assertThat(positionSlotB.getId()).isGreaterThan(0),
            () -> assertThat(positionSlotRepository.existsById(positionSlotA.getId())).isTrue(),
            () -> assertThat(positionSlotRepository.existsById(positionSlotB.getId())).isTrue()
        );
    }

    private void createVolunteers() {
        var volunteers = new ArrayList<Volunteer>();
        volunteerA = Volunteer.builder()
            .id("11111")
            .volunteeringPlans(Set.of(shiftPlanA))
            .build();
        volunteers.add(volunteerA);

        volunteerB = Volunteer.builder()
            .id("22222")
            .volunteeringPlans(Set.of(shiftPlanA))
            .build();
        volunteers.add(volunteerB);

        volunteerRepository.saveAll(volunteers);
        assertAll(
            () -> assertThat(volunteerA.getId()).isNotNull(),
            () -> assertThat(volunteerB.getId()).isNotNull(),
            () -> assertThat(volunteerA.getVolunteeringPlans()).contains(shiftPlanA),
            () -> assertThat(volunteerB.getVolunteeringPlans()).contains(shiftPlanA),
            () -> assertThat(volunteerRepository.existsById(ConvertUtil.idToLong(volunteerA.getId()))).isTrue(),
            () -> assertThat(volunteerRepository.existsById(ConvertUtil.idToLong(volunteerB.getId()))).isTrue()
        );
    }

    private void createAssignments() {
        assignmentA = Assignment.builder()
            .id(new AssignmentId(positionSlotA.getId(), volunteerA.getId()))
            .positionSlot(positionSlotA)
            .assignedVolunteer(volunteerA)
            .status(AssignmentStatus.ACCEPTED)
            .acceptedRewardPoints(40)
            .build();

        assignmentRepository.save(assignmentA);

        assertAll(
            () -> assertThat(assignmentA.getId()).isNotNull(),
            () -> assertThat(assignmentA.getPositionSlot()).isEqualTo(positionSlotA),
            () -> assertThat(assignmentA.getAssignedVolunteer()).isEqualTo(volunteerA),
            () -> assertThat(assignmentA.getStatus()).isEqualTo(AssignmentStatus.ACCEPTED)
        );
    }

    @AfterEach
    void tearDown() {
        assignmentRepository.deleteAll();
        positionSlotRepository.deleteAll();
        eventRepository.deleteAll();
        shiftPlanRepository.deleteAll();
        shiftRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    @Test
    void cloneEventAsNonAdminReturnsForbidden() {
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            EVENT_CLONE_PATH.formatted(eventA.getId()),
            "",
            FORBIDDEN.getStatusCode(),
            "Access Denied",
            true,
            volunteerA.getId()
        );
    }

    @Test
    void cloneEventByNonExistingIdReturnsNotFound() {
        doRequestAsAdminAndAssertMessage(
            Method.POST,
            EVENT_CLONE_PATH.formatted(9999),
            "",
            404,
            "Event not found.",
            true
        );
    }
}
