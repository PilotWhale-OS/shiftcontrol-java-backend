package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {
    @Mock
    private EventDao eventDao;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private UserDirectoryService userDirectoryService;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    @Test
    void getLeaderBoard_aggregatesHoursAndUsesDirectoryUsersForNames() {
        var user1 = volunteer("user-1");
        var user2 = volunteer("user-2");

        var shift1 = shift(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T12:00:00Z"), assignment(user1));
        var shift2 = shift(Instant.parse("2026-01-01T13:00:00Z"), Instant.parse("2026-01-01T14:00:00Z"), assignment(user1));
        var shift3 = shift(Instant.parse("2026-01-01T15:00:00Z"), Instant.parse("2026-01-01T16:00:00Z"), assignment(user2));

        var plan = new ShiftPlan();
        plan.setShifts(List.of(shift1, shift2, shift3));

        var event = new Event();
        event.setId(9L);
        event.setShiftPlans(List.of(plan));

        when(eventDao.findById(9L)).thenReturn(Optional.of(event));
        when(userDirectoryService.getUserByIds(Set.of("user-1", "user-2"))).thenReturn(List.of(
            directoryUser("user-1", "Alice", "Admin"),
            directoryUser("user-2", "Bob", "Builder")
        ));

        var result = leaderboardService.getLeaderBoard(9L, "user-1");

        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getOwnRank()).isNotNull();
        assertThat(result.getOwnRank().getRank()).isEqualTo(1);
        assertThat(result.getOwnRank().getHours()).isEqualTo(3);
        assertThat(result.getOwnRank().getFirstName()).isEqualTo("Alice");
        assertThat(result.getRanks())
            .extracting("rank", "hours", "firstName", "lastName")
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, 3L, "Alice", "Admin"),
                org.assertj.core.groups.Tuple.tuple(2, 1L, "Bob", "Builder")
            );

        verify(securityHelper).assertUserIsAllowedToAccessEvent(event);
    }

    private static Volunteer volunteer(String id) {
        var volunteer = new Volunteer();
        volunteer.setId(id);
        return volunteer;
    }

    private static Assignment assignment(Volunteer volunteer) {
        var assignment = new Assignment();
        assignment.setAssignedVolunteer(volunteer);
        return assignment;
    }

    private static Shift shift(Instant start, Instant end, Assignment assignment) {
        var slot = new PositionSlot();
        slot.setAssignments(List.of(assignment));

        var shift = new Shift();
        shift.setStartTime(start);
        shift.setEndTime(end);
        shift.setSlots(List.of(slot));

        slot.setShift(shift);
        var assignmentPositionSlot = new PositionSlot();
        assignmentPositionSlot.setShift(shift);
        assignment.setPositionSlot(assignmentPositionSlot);
        return shift;
    }

    private static DirectoryUser directoryUser(String id, String firstName, String lastName) {
        return new DirectoryUser(id, id, firstName, lastName, id + "@example.com", "https://cdn.example.test/profiles/" + id + ".png", UserType.ASSIGNED);
    }
}
