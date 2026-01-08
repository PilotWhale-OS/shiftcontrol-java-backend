package at.shiftcontrol.shiftservice.integration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.integration.config.RestITBase;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.repo.EventRepository;
import at.shiftcontrol.shiftservice.repo.PositionSlotRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanInviteRepository;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import at.shiftcontrol.shiftservice.repo.VolunteerRepository;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.type.LockStatus;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;
import io.restassured.http.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ShiftPlanIT extends RestITBase {
    private static final String INVITE_PATH = "shift-plans/%d/invites";
    private static final String JOIN_PATH = "/join";

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

    @Autowired
    ShiftPlanInviteRepository shiftPlanInviteRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserAttributeProvider userAttributeProvider;

    private Event eventA;
    private ShiftPlan shiftPlanA;
    private Shift shiftA, shiftB;

    private Volunteer volunteerNotJoined, volunteerJoinedAsVolunteerOnly, volunteerJoinedAsPlannerOnly;

    private PositionSlot positionSlotA, positionSlotB;

    private Role roleA, roleB, roleC;

    @BeforeEach
    void setUp() {
        shiftPlanInviteRepository.deleteAll();
        roleRepository.deleteAll();
        positionSlotRepository.deleteAll();
        shiftRepository.deleteAll();
        shiftPlanRepository.deleteAll();
        eventRepository.deleteAll();
        volunteerRepository.deleteAll();
        assignmentRepository.deleteAll();

        createEvents();
        createShiftPlans();
        createRoles();
        createShifts();
        createPositionSlots();
        createVolunteers();
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

    private void createRoles() {
        var roles = new ArrayList<Role>();

        roleA = Role.builder()
            .name("RoleA")
            .shiftPlan(shiftPlanA)
            .description("DescriptionA")
            .selfAssignable(true)
            .rewardPointsPerMinute(2)
            .build();
        roles.add(roleA);

        roleB = Role.builder()
            .name("RoleB")
            .shiftPlan(shiftPlanA)
            .description("DescriptionB")
            .selfAssignable(true)
            .rewardPointsPerMinute(3)
            .build();
        roles.add(roleB);

        roleC = Role.builder()
            .name("RoleC")
            .shiftPlan(shiftPlanA)
            .description("DescriptionC")
            .selfAssignable(true)
            .rewardPointsPerMinute(5)
            .build();
        roles.add(roleC);

        roleRepository.saveAll(roles);

        assertAll(
            () -> assertThat(roleA.getId()).isGreaterThan(0),
            () -> assertThat(roleB.getId()).isGreaterThan(0),
            () -> assertThat(roleC.getId()).isGreaterThan(0),
            () -> assertThat(roleRepository.existsById(roleA.getId())).isTrue(),
            () -> assertThat(roleRepository.existsById(roleB.getId())).isTrue(),
            () -> assertThat(roleRepository.existsById(roleC.getId())).isTrue()
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
        volunteerNotJoined = Volunteer.builder()
            .id("11111")
            .build();
        volunteers.add(volunteerNotJoined);

        volunteerJoinedAsVolunteerOnly = Volunteer.builder()
            .id("22222")
            .build();
        shiftPlanA.addPlanVolunteer(volunteerJoinedAsVolunteerOnly);
        volunteers.add(volunteerJoinedAsVolunteerOnly);

        volunteerJoinedAsPlannerOnly = Volunteer.builder()
            .id("33333")
            .build();
        shiftPlanA.addPlanPlanner(volunteerJoinedAsPlannerOnly);
        volunteers.add(volunteerJoinedAsPlannerOnly);

        volunteerRepository.saveAll(volunteers);
        assertAll(
            () -> assertThat(volunteerNotJoined.getId()).isNotNull(),
            () -> assertThat(volunteerJoinedAsVolunteerOnly.getId()).isNotNull(),
            () -> assertThat(volunteerJoinedAsPlannerOnly.getId()).isNotNull(),
            () -> assertThat(volunteerRepository.existsById(ConvertUtil.idToLong(volunteerNotJoined.getId()))).isTrue(),
            () -> assertThat(volunteerRepository.existsById(ConvertUtil.idToLong(volunteerJoinedAsVolunteerOnly.getId()))).isTrue(),
            () -> assertThat(volunteerRepository.existsById(ConvertUtil.idToLong(volunteerJoinedAsPlannerOnly.getId()))).isTrue(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan("11111", shiftPlanA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.isPlannerInShiftPlan("11111", shiftPlanA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan("22222", shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.isPlannerInShiftPlan("22222", shiftPlanA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.isPlannerInShiftPlan("33333", shiftPlanA.getId())).isTrue()
        );
    }

    @AfterEach
    void tearDown() {
        shiftPlanInviteRepository.deleteAll();
        assignmentRepository.deleteAll();
        positionSlotRepository.deleteAll();

        shiftRepository.deleteAll();
        volunteerRepository.deleteAll();
        roleRepository.deleteAll();
        shiftPlanRepository.deleteAll();

        eventRepository.deleteAll();

        // invalidate caches after each test
        userAttributeProvider.invalidateUserCache(volunteerNotJoined.getId());
        userAttributeProvider.invalidateUserCache(volunteerJoinedAsVolunteerOnly.getId());
        userAttributeProvider.invalidateUserCache(volunteerJoinedAsPlannerOnly.getId());
    }

    @Test
    void createVolunteerInviteCodeAsNotJoinedReturnsForbidden() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .build();
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            FORBIDDEN.getStatusCode(),
            "User is not a planner in shift plan.",
            volunteerNotJoined.getId()
        );
    }

    @Test
    void createPlannerInviteCodeAsNotJoinedReturnsForbidden() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .build();
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            FORBIDDEN.getStatusCode(),
            "Only admins can create planner join invite codes.",
            volunteerNotJoined.getId()
        );
    }

    @Test
    void createVolunteerInviteCodeAsVolunteerOnlyReturnsForbidden() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .build();
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            FORBIDDEN.getStatusCode(),
            "User is not a planner in shift plan.",
            volunteerJoinedAsVolunteerOnly.getId()
        );
    }

    @Test
    void createPlannerInviteCodeAsVolunteerOnlyReturnsForbidden() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .build();
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            FORBIDDEN.getStatusCode(),
            "Only admins can create planner join invite codes.",
            volunteerJoinedAsVolunteerOnly.getId()
        );
    }

    @Test
    void createVolunteerInviteCodeAsPlannerOnlySucceeds() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .build();

        var result = postRequestAsAssigned(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            ShiftPlanInviteCreateResponseDto.class,
            volunteerJoinedAsPlannerOnly.getId()
        );

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getCode()).isNotNull(),
            () -> assertThat(result.getCode()).isNotBlank(),
            () -> assertThat(result.getType()).isEqualTo(ShiftPlanInviteType.VOLUNTEER_JOIN)
        );
    }

    @Test
    void createPlannerInviteCodeAsPlannerOnlyReturnsForbidden() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .build();
        doRequestAsAssignedAndAssertMessage(
            Method.POST,
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            FORBIDDEN.getStatusCode(),
            "Only admins can create planner join invite codes.",
            volunteerJoinedAsPlannerOnly.getId()
        );
    }

    @Test
    void createPlannerInviteCodeAsAdminSucceeds() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .build();

        var result = postRequestAsAdmin(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            ShiftPlanInviteCreateResponseDto.class
        );

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getCode()).isNotNull(),
            () -> assertThat(result.getCode()).isNotBlank(),
            () -> assertThat(result.getType()).isEqualTo(ShiftPlanInviteType.PLANNER_JOIN)
        );
    }

    @Test
    void createVolunteerInviteCodeAsPlannerAndJoinAsNotJoinedSucceeds() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .autoAssignRoleIds(List.of(String.valueOf(roleA.getId()), String.valueOf(roleB.getId())))
            .build();

        var resultInvite = postRequestAsAssigned(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            ShiftPlanInviteCreateResponseDto.class,
            volunteerJoinedAsPlannerOnly.getId()
        );


        assertAll(
            () -> assertThat(resultInvite).isNotNull(),
            () -> assertThat(resultInvite.getCode()).isNotNull(),
            () -> assertThat(resultInvite.getCode()).isNotBlank(),
            () -> assertThat(resultInvite.getType()).isEqualTo(ShiftPlanInviteType.VOLUNTEER_JOIN),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isFalse()
        );

        // Now join as volunteer using the invite code
        var joinRequestDto = ShiftPlanJoinRequestDto.builder()
            .inviteCode(resultInvite.getCode())
            .build();

        var resultJoin = postRequestAsAssigned(
            JOIN_PATH,
            joinRequestDto,
            ShiftPlanJoinOverviewDto.class,
            volunteerNotJoined.getId()
        );

        assertAll(
            () -> assertThat(resultJoin).isNotNull(),
            () -> assertThat(resultJoin.isJoined()).isTrue(),
            () -> assertThat(resultJoin.getAttendingVolunteerCount()).isEqualTo(2),
            () -> assertThat(resultJoin.getEventDto()).isNotNull(),
            () -> assertThat(resultJoin.getInviteDto()).isNotNull(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isTrue()
        );
    }

    @Test
    void createTwoVolunteerInviteCodesAsPlannerAndJoinBothAsNotJoinedExtendsRolesSucceeds() {
        // Create first invite
        var requestDto1 = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .autoAssignRoleIds(List.of(String.valueOf(roleA.getId()), String.valueOf(roleB.getId())))
            .build();

        var resultInvite1 = postRequestAsAssigned(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto1,
            ShiftPlanInviteCreateResponseDto.class,
            volunteerJoinedAsPlannerOnly.getId()
        );


        assertAll(
            () -> assertThat(resultInvite1).isNotNull(),
            () -> assertThat(resultInvite1.getCode()).isNotNull(),
            () -> assertThat(resultInvite1.getCode()).isNotBlank(),
            () -> assertThat(resultInvite1.getType()).isEqualTo(ShiftPlanInviteType.VOLUNTEER_JOIN),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isFalse(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isFalse()
        );

        // Join first time
        var joinRequestDto1 = ShiftPlanJoinRequestDto.builder()
            .inviteCode(resultInvite1.getCode())
            .build();

        var resultJoin1 = postRequestAsAssigned(
            JOIN_PATH,
            joinRequestDto1,
            ShiftPlanJoinOverviewDto.class,
            volunteerNotJoined.getId()
        );

        assertAll(
            () -> assertThat(resultJoin1).isNotNull(),
            () -> assertThat(resultJoin1.isJoined()).isTrue(),
            () -> assertThat(resultJoin1.getAttendingVolunteerCount()).isEqualTo(2),
            () -> assertThat(resultJoin1.getEventDto()).isNotNull(),
            () -> assertThat(resultJoin1.getInviteDto()).isNotNull(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isTrue()
        );

        // Create second invite
        var requestDto2 = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.VOLUNTEER_JOIN)
            .autoAssignRoleIds(List.of(String.valueOf(roleC.getId())))
            .build();

        var resultInvite2 = postRequestAsAssigned(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto2,
            ShiftPlanInviteCreateResponseDto.class,
            volunteerJoinedAsPlannerOnly.getId()
        );

        assertAll(
            () -> assertThat(resultInvite2).isNotNull(),
            () -> assertThat(resultInvite2.getCode()).isNotNull(),
            () -> assertThat(resultInvite2.getCode()).isNotBlank(),
            () -> assertThat(resultInvite2.getType()).isEqualTo(ShiftPlanInviteType.VOLUNTEER_JOIN),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleC.getId())).isFalse()
        );

        // Join second time
        var joinRequestDto2 = ShiftPlanJoinRequestDto.builder()
            .inviteCode(resultInvite2.getCode())
            .build();

        var resultJoin2 = postRequestAsAssigned(
            JOIN_PATH,
            joinRequestDto2,
            ShiftPlanJoinOverviewDto.class,
            volunteerNotJoined.getId()
        );

        assertAll(
            () -> assertThat(resultJoin2).isNotNull(),
            () -> assertThat(resultJoin2.isJoined()).isFalse(),
            () -> assertThat(resultJoin2.getAttendingVolunteerCount()).isEqualTo(2),
            () -> assertThat(resultJoin2.getEventDto()).isNotNull(),
            () -> assertThat(resultJoin2.getInviteDto()).isNotNull(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerNotJoined.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleB.getId())).isTrue(),
            () -> assertThat(volunteerRepository.hasUserRole(volunteerNotJoined.getId(), roleC.getId())).isTrue()
        );
    }

    @Test
    void createPlannerInviteCodesAsAdminAndJoinAsVolunteerOnlyUpgradesToPlannerSucceeds() {
        var requestDto = ShiftPlanInviteCreateRequestDto.builder()
            .type(ShiftPlanInviteType.PLANNER_JOIN)
            .build();

        var resultInvite = postRequestAsAdmin(
            INVITE_PATH.formatted(shiftPlanA.getId()),
            requestDto,
            ShiftPlanInviteCreateResponseDto.class
        );

        assertAll(
            () -> assertThat(resultInvite).isNotNull(),
            () -> assertThat(resultInvite.getCode()).isNotNull(),
            () -> assertThat(resultInvite.getCode()).isNotBlank(),
            () -> assertThat(resultInvite.getType()).isEqualTo(ShiftPlanInviteType.PLANNER_JOIN),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerJoinedAsVolunteerOnly.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.isPlannerInShiftPlan(volunteerJoinedAsVolunteerOnly.getId(), shiftPlanA.getId())).isFalse()
        );

        // Now join as planner using the invite code
        var joinRequestDto = ShiftPlanJoinRequestDto.builder()
            .inviteCode(resultInvite.getCode())
            .build();

        var resultJoin = postRequestAsAssigned(
            JOIN_PATH,
            joinRequestDto,
            ShiftPlanJoinOverviewDto.class,
            volunteerJoinedAsVolunteerOnly.getId()
        );

        assertAll(
            () -> assertThat(resultJoin).isNotNull(),
            () -> assertThat(resultJoin.isJoined()).isTrue(),
            () -> assertThat(resultJoin.getAttendingVolunteerCount()).isEqualTo(1), // nothing changes
            () -> assertThat(resultJoin.getEventDto()).isNotNull(),
            () -> assertThat(resultJoin.getInviteDto()).isNotNull(),
            () -> assertThat(volunteerRepository.isVolunteerInShiftPlan(volunteerJoinedAsVolunteerOnly.getId(), shiftPlanA.getId())).isTrue(),
            () -> assertThat(volunteerRepository.isPlannerInShiftPlan(volunteerJoinedAsVolunteerOnly.getId(), shiftPlanA.getId())).isTrue()
        );
    }
}
