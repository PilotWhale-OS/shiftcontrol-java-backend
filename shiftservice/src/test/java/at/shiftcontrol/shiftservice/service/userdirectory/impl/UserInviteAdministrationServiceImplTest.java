package at.shiftcontrol.shiftservice.service.userdirectory.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.common.UniqueCodeGenerator;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.type.LockStatus;
import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteCreateDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteSearchDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteShiftPlanAccessCreateDto;
import at.shiftcontrol.shiftservice.repo.ShiftPlanRepository;
import at.shiftcontrol.shiftservice.repo.role.RoleRepository;
import at.shiftcontrol.shiftservice.repo.userdirectory.UserInviteRepository;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteClaimService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInviteAdministrationServiceImplTest {
    @Mock
    private UserInviteRepository userInviteRepository;

    @Mock
    private ShiftPlanRepository shiftPlanRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UniqueCodeGenerator uniqueCodeGenerator;

    @Mock
    private UserInviteClaimService userInviteClaimService;

    @InjectMocks
    private UserInviteAdministrationServiceImpl service;

    @Test
    void getAllInvites_filtersByNameAndStatus() {
        UserInvite pendingAlice = invite(1, "alice@example.com", "Alice", UserInviteStatus.PENDING, Instant.parse("2026-06-17T10:00:00Z"));
        when(userInviteRepository.searchInviteIds(UserInviteStatus.PENDING, "%ali%", PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(1L), PageRequest.of(0, 10), 1));
        when(userInviteRepository.findAllDetailedByIdIn(List.of(1L))).thenReturn(List.of(pendingAlice));

        var result = service.getAllInvites(0, 10, UserInviteSearchDto.builder().name("ali").status(UserInviteStatus.PENDING).build());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).singleElement()
            .extracting(dto -> dto.getEmail(), dto -> dto.getStatus())
            .containsExactly("alice@example.com", UserInviteStatus.PENDING);
    }

    @Test
    void createInvite_rejectsRolesWithoutMatchingPlanAccess() {
        ShiftPlan shiftPlan = shiftPlan(9L, "Planner Access");
        Role role = role(11L, "Dispatch", shiftPlan);
        when(uniqueCodeGenerator.generateUnique(anyString(), anyInt(), anyInt(), any())).thenReturn("INVITE001");
        when(roleRepository.findAllById(anySet())).thenReturn(List.of(role));
        when(shiftPlanRepository.findAllById(anySet())).thenReturn(List.of());

        UserInviteCreateDto createDto = UserInviteCreateDto.builder()
            .email("future@example.com")
            .roleIds(List.of("11"))
            .build();

        assertThatThrownBy(() -> service.createInvite(createDto))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("missing volunteer or planner access");
    }

    @Test
    void revokeInvite_marksPendingInviteRevoked() {
        UserInvite invite = invite(5, "future@example.com", "Future", UserInviteStatus.PENDING, Instant.parse("2026-06-17T10:00:00Z"));
        when(userInviteRepository.findById(5L)).thenReturn(Optional.of(invite));

        service.revokeInvite(5L);

        assertThat(invite.getStatus()).isEqualTo(UserInviteStatus.REVOKED);
        assertThat(invite.getRevokedAt()).isNotNull();
        verify(userInviteRepository).save(invite);
        verify(userInviteClaimService).invalidatePendingInviteEmailCache("future@example.com");
    }

    @Test
    void createInvite_persistsPendingAccessesAndRoles() {
        ShiftPlan shiftPlan = shiftPlan(9L, "Planner Access");
        Role role = role(11L, "Dispatch", shiftPlan);
        when(uniqueCodeGenerator.generateUnique(anyString(), anyInt(), anyInt(), any())).thenReturn("INVITE001");
        when(roleRepository.findAllById(anySet())).thenReturn(List.of(role));
        when(shiftPlanRepository.findAllById(anySet())).thenReturn(List.of(shiftPlan));
        when(userInviteRepository.save(any(UserInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserInviteCreateDto createDto = UserInviteCreateDto.builder()
            .email("future@example.com")
            .roleIds(List.of("11"))
            .shiftPlanAccesses(List.of(UserInviteShiftPlanAccessCreateDto.builder()
                .shiftPlanId("9")
                .accessType(UserInviteShiftPlanAccessType.PLANNER)
                .build()))
            .build();

        service.createInvite(createDto);

        ArgumentCaptor<UserInvite> inviteCaptor = ArgumentCaptor.forClass(UserInvite.class);
        verify(userInviteRepository).save(inviteCaptor.capture());
        UserInvite savedInvite = inviteCaptor.getValue();
        assertThat(savedInvite.getCode()).isEqualTo("INVITE001");
        assertThat(savedInvite.getStatus()).isEqualTo(UserInviteStatus.PENDING);
        assertThat(savedInvite.getPendingRoles()).hasSize(1);
        assertThat(savedInvite.getPendingShiftPlanAccesses()).hasSize(1);
        verify(userInviteClaimService).invalidatePendingInviteEmailCache("future@example.com");
    }

    private static UserInvite invite(long id, String email, String firstName, UserInviteStatus status, Instant createdAt) {
        return UserInvite.builder()
            .id(id)
            .code("code-" + id)
            .email(email)
            .firstName(firstName)
            .status(status)
            .createdAt(createdAt)
            .build();
    }

    private static ShiftPlan shiftPlan(long id, String name) {
        return ShiftPlan.builder()
            .id(id)
            .name(name)
            .event(Event.builder().id(3L).name("Event").build())
            .lockStatus(LockStatus.SELF_SIGNUP)
            .defaultNoRolePointsPerMinute(0)
            .build();
    }

    private static Role role(long id, String name, ShiftPlan shiftPlan) {
        return Role.builder()
            .id(id)
            .name(name)
            .shiftPlan(shiftPlan)
            .description(name)
            .selfAssignable(false)
            .rewardPointsPerMinute(1)
            .build();
    }
}
