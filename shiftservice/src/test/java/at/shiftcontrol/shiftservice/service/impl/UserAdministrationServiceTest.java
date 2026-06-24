package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.VolunteerService;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdministrationServiceTest {
    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private ShiftPlanDao shiftPlanDao;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private UserAttributeProvider userAttributeProvider;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private RoleDao roleDao;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private AssignmentSwitchRequestDao assignmentSwitchRequestDao;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private VolunteerService volunteerService;

    @InjectMocks
    private UserAdministrationServiceImpl userAdministrationService;

    @Test
    void getAllUsers_filtersAndPaginatesDirectoryUsers() {
        var alice = user("1", "alice", "Alice", "Anderson");
        var bob = user("2", "bobby", "Bob", "Builder");

        when(userDirectoryService.searchUsers(0, 2, UserSearchDto.builder().name("o").build()))
            .thenReturn(at.shiftcontrol.lib.dto.PaginationDto.<DirectoryUser>builder()
                .page(0)
                .pages(2)
                .total(3)
                .items(List.of(alice, bob))
                .build());
        when(volunteerDao.findAllByVolunteerIds(List.of("1", "2"))).thenReturn(List.of());

        var result = userAdministrationService.getAllUsers(0, 2, UserSearchDto.builder().name("o").build());

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
            .extracting(dto -> dto.getVolunteer().getId(), dto -> dto.getVolunteer().getFirstName())
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("1", "Alice"),
                org.assertj.core.groups.Tuple.tuple("2", "Bob")
            );
    }

    @Test
    void getUser_doesNotCreateVolunteerWhenNoLocalMembershipExists() {
        var user = user("user-1", "user.one", "User", "One");
        when(userDirectoryService.getUserById("user-1")).thenReturn(user);
        when(volunteerDao.findById("user-1")).thenReturn(Optional.empty());

        var result = userAdministrationService.getUser("user-1");

        assertThat(result.getVolunteer().getId()).isEqualTo("user-1");
        assertThat(result.getPlanningPlans()).isEmpty();
        assertThat(result.getVolunteeringPlans()).isEmpty();
        verify(volunteerService, never()).getOrCreate("user-1");
    }

    @Test
    void createVolunteer_requiresKnownLocalUserBeforeMaterializingVolunteerState() {
        var user = user("user-1", "user.one", "User", "One");
        when(userDirectoryService.getUserById("user-1")).thenReturn(user);
        when(volunteerService.createVolunteer("user-1")).thenReturn(at.shiftcontrol.lib.entity.Volunteer.builder()
            .id("user-1")
            .planningPlans(java.util.Set.of())
            .volunteeringPlans(java.util.Set.of())
            .lockedPlans(java.util.Set.of())
            .roles(java.util.Set.of())
            .notificationSettings(java.util.Set.of())
            .build());

        var result = userAdministrationService.createVolunteer("user-1");

        assertThat(result.getVolunteer().getId()).isEqualTo("user-1");
        verify(userDirectoryService, atLeastOnce()).getUserById("user-1");
        verify(volunteerService).createVolunteer("user-1");
    }

    private static DirectoryUser user(String id, String username, String firstName, String lastName) {
        return new DirectoryUser(id, username, firstName, lastName, username + "@example.com", "https://cdn.example.test/profiles/" + id + ".png", false);
    }
}
