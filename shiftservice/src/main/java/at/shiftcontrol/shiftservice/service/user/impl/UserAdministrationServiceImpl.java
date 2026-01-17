package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;
import at.shiftcontrol.shiftservice.mapper.PaginationMapper;
import at.shiftcontrol.shiftservice.mapper.UserAssemblingMapper;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {
    private final VolunteerDao volunteerDao;
    private final ShiftPlanDao shiftPlanDao;
    private final KeycloakUserService keycloakUserService;
    private final UserAttributeProvider userAttributeProvider;
    private final SecurityHelper securityHelper;
    private final UserAssemblingMapper userAssemblingMapper;
    private final RoleDao roleDao;

    @Override
    @AdminOnly
    public PaginationDto<UserEventDto> getAllUsers(int page, int size, UserSearchDto searchDto) {
        var volunteers = volunteerDao.findAll(page, size);

        var users = keycloakUserService.getAllAssigned();
        if (searchDto.getName() != null && !searchDto.getName().isEmpty()) {
            var nameLower = searchDto.getName().toLowerCase().trim();
            users = users.stream().filter(x ->
                    x.getUsername().contains(nameLower)
                        || x.getFirstName().contains(nameLower)
                        || x.getLastName().contains(nameLower)
                )
                .toList();
        }
        return PaginationMapper.toPaginationDto(size, page, users.size(), UserAssemblingMapper.toUserEventDtoForUsers(volunteers, users));
    }

    @Override
    public PaginationDto<UserPlanDto> getAllPlanUsers(Long shiftPlanId, int page, int size) {
        var volunteers = volunteerDao.findAll(page, size);
        var totalSize = volunteerDao.findAllSize();
        return PaginationMapper.toPaginationDto(size, page, totalSize, getUserPlanDtos(shiftPlanId, volunteers));
    }

    @Override
    @AdminOnly
    public UserEventDto getUser(String userId) {
        var volunteer = volunteerDao.getById(userId);
        return userAssemblingMapper.toUserEventDto(volunteer);
    }

    @Override
    public UserPlanDto getPlanUser(Long shiftPlanId, String userId) {
        var volunteer = volunteerDao.getById(userId);
        return userAssemblingMapper.toUserPlanDto(volunteer, shiftPlanId);
    }

    @Override
    @Transactional
    @AdminOnly
    public UserEventDto updateUser(String userId, UserEventUpdateDto updateDto) {
        var volunteer = volunteerDao.getById(userId);
        assertPlansChanged(volunteer, updateDto);

        var volunteerToAdd = toAddPlans(updateDto.getVolunteeringPlans(), volunteer.getVolunteeringPlans());
        var planningToAdd = toAddPlans(updateDto.getPlanningPlans(), volunteer.getPlanningPlans());
        addPlans(volunteer, volunteerToAdd, planningToAdd);

        var volunteerToRemove = toRemovePlans(updateDto.getVolunteeringPlans(), volunteer.getVolunteeringPlans());
        var planningToRemove = toRemovePlans(updateDto.getPlanningPlans(), volunteer.getPlanningPlans());
        removePlans(volunteer, volunteerToRemove, planningToRemove);


        var user = keycloakUserService.getUserById(volunteer.getId());
        userAttributeProvider.invalidateUserCache(userId);
        return UserAssemblingMapper.toUserEventDto(volunteer, user);
    }

    @Override
    public UserPlanDto updatePlanUser(Long shiftPlanId, String userId, UserPlanUpdateDto updateDto) {
        var volunteer = volunteerDao.getById(userId);
        assertRolesChanged(volunteer, updateDto);

        Set<Long> currentRoles = volunteer.getRoles().stream().map(Role::getId).collect(Collectors.toSet());

        var rolesToAdd = toAdd(updateDto.getRoles(), currentRoles);
        addRoles(volunteer, rolesToAdd);

        var rolesToRemove = toRemove(updateDto.getRoles(), currentRoles);
        removeRoles(volunteer, rolesToRemove);

        var user = keycloakUserService.getUserById(volunteer.getId());
        userAttributeProvider.invalidateUserCache(userId);
        return UserAssemblingMapper.toUserPlanDto(volunteer, user, shiftPlanId);
    }

    @Override
    @Transactional
    public UserEventDto lockUser(String userId, long shiftPlanId) {
        securityHelper.assertUserIsPlanner(shiftPlanId);
        var volunteer = volunteerDao.getById(userId);
        var plan = shiftPlanDao.getById(shiftPlanId);
        securityHelper.assertVolunteerIsNotLockedInPlan(plan, volunteer);
        if (volunteer.getLockedPlans() == null) {
            volunteer.setLockedPlans(new HashSet<>());
        }
        volunteer.getLockedPlans().add(plan);

        userAttributeProvider.invalidateUserCache(userId);
        return userAssemblingMapper.toUserEventDto(volunteer);
    }

    @Override
    @Transactional
    public UserEventDto unLockuser(String userId, long shiftPlanId) {
        securityHelper.assertUserIsPlanner(shiftPlanId);
        var volunteer = volunteerDao.getById(userId);
        var plan = shiftPlanDao.getById(shiftPlanId);
        securityHelper.assertVolunteerIsLockedInPlan(plan, volunteer);
        volunteer.getLockedPlans().remove(plan);

        userAttributeProvider.invalidateUserCache(userId);
        return userAssemblingMapper.toUserEventDto(volunteer);
    }

    @Override
    public Collection<UserPlanDto> bulkAddRoles(long shiftPlanId, UserPlanBulkDto updateDto) {
        var volunteers = volunteerDao.findAllByVolunteerIds(updateDto.getVolunteers());
        var roles = roleDao.findAllById(updateDto.getRoles().stream().map(ConvertUtil::idToLong).toList());
        for (Volunteer v : volunteers) {
            for (Role role : roles) {
                if (!v.getRoles().contains(role)) {
                    v.getRoles().add(role);
                }
            }
        }
        return getUserPlanDtos(shiftPlanId, volunteers);
    }

    @Override
    public Collection<UserPlanDto> bulkRemoveRoles(long shiftPlanId, UserPlanBulkDto updateDto) {
        var volunteers = volunteerDao.findAllByVolunteerIds(updateDto.getVolunteers());
        var roles = roleDao.findAllById(updateDto.getRoles().stream().map(ConvertUtil::idToLong).toList());
        for (Volunteer v : volunteers) {
            v.getRoles().removeAll(roles);
        }

        return getUserPlanDtos(shiftPlanId, volunteers);
    }

    @Override
    public Collection<UserEventDto> bulkAddVolunteeringPlans(UserEventBulkDto updateDto) {
        var volunteers = volunteerDao.findAllByVolunteerIds(updateDto.getVolunteers());
        var plans = shiftPlanDao.getByIds(updateDto.getPlans().stream().map(ConvertUtil::idToLong).collect(Collectors.toSet()));
        for (Volunteer v : volunteers) {
            for (ShiftPlan plan : plans) {
                if (!v.getVolunteeringPlans().contains(plan)) {
                    v.getVolunteeringPlans().add(plan);
                }
            }
        }
        return getUserEventDtos(volunteers);
    }

    @Override
    public Collection<UserEventDto> bulkRemoveVolunteeringPlans(UserEventBulkDto updateDto) {
        var volunteers = volunteerDao.findAllByVolunteerIds(updateDto.getVolunteers());
        var plans = shiftPlanDao.getByIds(updateDto.getPlans().stream().map(ConvertUtil::idToLong).collect(Collectors.toSet()));
        for (Volunteer v : volunteers) {
            v.getVolunteeringPlans().removeAll(plans);
        }
        return getUserEventDtos(volunteers);
    }

    private @NonNull Collection<UserPlanDto> getUserPlanDtos(long shiftPlanId, Collection<Volunteer> volunteers) {
        var users = keycloakUserService.getUserByIds(volunteers.stream().map(Volunteer::getId).toList());
        userAttributeProvider.invalidateUserCaches(users.stream().map(UserRepresentation::getId).toList());
        return UserAssemblingMapper.toUserPlanDto(volunteers, users, shiftPlanId);
    }

    private @NonNull Collection<UserEventDto> getUserEventDtos(Collection<Volunteer> volunteers) {
        var users = keycloakUserService.getUserByIds(volunteers.stream().map(Volunteer::getId).toList());
        return UserAssemblingMapper.toUserEventDto(volunteers, users);
    }

    private void addPlans(Volunteer volunteer, Set<Long> volunteerToAdd, Set<Long> planningToAdd) {
        if (volunteerToAdd != null && !volunteerToAdd.isEmpty()) {
            volunteer.setVolunteeringPlans(initIfNull(volunteer.getVolunteeringPlans()));
            volunteer.getVolunteeringPlans().addAll(shiftPlanDao.getByIds(volunteerToAdd));
        }
        if (planningToAdd != null && !planningToAdd.isEmpty()) {
            volunteer.setPlanningPlans(initIfNull(volunteer.getPlanningPlans()));
            volunteer.getPlanningPlans().addAll(shiftPlanDao.getByIds(planningToAdd));
        }
    }

    private void removePlans(Volunteer volunteer, Set<Long> volunteerToRemove, Set<Long> planningToRemove) {
        if (volunteer.getVolunteeringPlans() != null) {
            volunteer.getVolunteeringPlans().removeIf(x -> volunteerToRemove.contains(x.getId()));
        }
        if (volunteer.getVolunteeringPlans() != null) {
            volunteer.getLockedPlans().removeIf(x -> volunteerToRemove.contains(x.getId()));
        }
        if (volunteer.getVolunteeringPlans() != null) {
            volunteer.getPlanningPlans().removeIf(x -> planningToRemove.contains(x.getId()));
        }
    }

    private void addRoles(Volunteer vol, Set<Long> rolesToAdd) {
        if (rolesToAdd != null && !rolesToAdd.isEmpty()) {
            vol.setRoles(initIfNull(vol.getRoles()));
            vol.getRoles().addAll(roleDao.getByIds(rolesToAdd));
        }
    }

    private void removeRoles(Volunteer volunteer, Set<Long> rolesToRemove) {
        if (volunteer.getRoles() != null) {
            volunteer.getRoles().removeIf(x -> rolesToRemove.contains(x.getId()));
        }
    }

    private Set<Long> toAddPlans(Collection<String> request, Collection<ShiftPlan> existingPlans) {
        var existingIds = existingPlans.stream().map(ShiftPlan::getId).collect(Collectors.toSet());
        return toAdd(request, existingIds);
    }

    private Set<Long> toAdd(Collection<String> request, Set<Long> existingIds) {
        var requestSet = request.stream().map(ConvertUtil::idToLong).collect(Collectors.toSet());
        requestSet.removeAll(existingIds);
        return requestSet;
    }

    private Set<Long> toRemovePlans(Collection<String> request, Collection<ShiftPlan> existingPlans) {
        var existingIds = existingPlans.stream().map(ShiftPlan::getId).collect(Collectors.toSet());
        return toRemove(request, existingIds);
    }

    private Set<Long> toRemove(Collection<String> request, Set<Long> existingIds) {
        var requestSet = request.stream().map(ConvertUtil::idToLong).collect(Collectors.toSet());
        existingIds.removeAll(requestSet);
        return existingIds;
    }

    private void assertPlansChanged(Volunteer volunteer, UserEventUpdateDto updateDto) {
        var currentVolunteeringPlans = volunteer.getVolunteeringPlans().stream().map(x -> String.valueOf(x.getId())).toList();
        var currentPlaningPlans = volunteer.getPlanningPlans().stream().map(x -> String.valueOf(x.getId())).toList();
        if (new HashSet<>(currentVolunteeringPlans).equals(new HashSet<>(updateDto.getVolunteeringPlans()))
            && new HashSet<>(currentPlaningPlans).equals(new HashSet<>(updateDto.getPlanningPlans()))) {
            throw new BadRequestException("Update does not change anything");
        }
    }

    private void assertRolesChanged(Volunteer volunteer, UserPlanUpdateDto updateDto) {
        var currentRoles = volunteer.getRoles().stream().map(x -> String.valueOf(x.getId())).toList();
        if (new HashSet<>(currentRoles).equals(new HashSet<>(updateDto.getRoles()))) {
            throw new BadRequestException("Update does not change anything");
        }
    }

    private static <T> Collection<T> initIfNull(Collection<T> set) {
        return set != null ? set : new HashSet<>();
    }
}
