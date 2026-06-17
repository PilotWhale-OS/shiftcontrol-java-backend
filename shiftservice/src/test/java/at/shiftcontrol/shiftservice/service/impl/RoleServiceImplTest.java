package at.shiftcontrol.shiftservice.service.impl;

import java.util.HashSet;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock
    private RoleDao roleDao;

    @Mock
    private ShiftPlanDao shiftPlanDao;

    @Mock
    private VolunteerDao volunteerDao;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private UserDirectoryService userDirectoryService;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createUserRoleAssignment_returnsVolunteerDtoFromDirectoryUser() {
        Event event = new Event();
        event.setId(3L);
        ShiftPlan shiftPlan = new ShiftPlan();
        shiftPlan.setId(11L);
        shiftPlan.setEvent(event);
        Role role = new Role();
        role.setId(7L);
        role.setShiftPlan(shiftPlan);
        role.setName("Check-In");
        role.setSelfAssignable(true);
        Volunteer volunteer = new Volunteer();
        volunteer.setId("user-1");
        volunteer.setRoles(new HashSet<>());

        when(roleDao.getById(7L)).thenReturn(role);
        when(volunteerDao.getById("user-1")).thenReturn(volunteer);
        when(userDirectoryService.getUserById("user-1")).thenReturn(new DirectoryUser(
            "user-1",
            "alice",
            "Alice",
            "Admin",
            "alice@example.com",
            UserType.ASSIGNED
        ));

        var result = roleService.createUserRoleAssignment(
            "user-1",
            UserRoleAssignmentAssignDto.builder().roleId("7").build()
        );

        assertThat(result.getId()).isEqualTo("user-1");
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getLastName()).isEqualTo("Admin");
        verify(volunteerDao).save(volunteer);
    }
}
