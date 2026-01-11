package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
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

    @Override
    @AdminOnly
    public Collection<UserEventDto> getAllUsers(long page, long size) {
        var volunteers = volunteerDao.findAll(page, size);
        var users = keycloakUserService.getUserByIds(volunteers.stream().map(Volunteer::getId).toList());
        return UserAssemblingMapper.toUserEventDto(volunteers, users);
    }

    @Override
    @AdminOnly
    public UserEventDto getUser(String userId) {
        var volunteer = volunteerDao.getById(userId);
        return userAssemblingMapper.toUserEventDto(volunteer);
    }

    @Override
    @Transactional
    @AdminOnly
    public UserEventDto updateUser(String userId, UserEventUpdateDto updateDto) {
        var volunteer = volunteerDao.getById(userId);
        assertPlansChanged(volunteer, updateDto);

        var volunteerToAdd = toAdd(updateDto.getVolunteeringPlans(), volunteer.getVolunteeringPlans());
        var planningToAdd = toAdd(updateDto.getPlanningPlans(), volunteer.getPlanningPlans());
        addPlans(volunteer, volunteerToAdd, planningToAdd);

        var volunteerToRemove = toRemove(updateDto.getVolunteeringPlans(), volunteer.getVolunteeringPlans());
        var planningToRemove = toRemove(updateDto.getPlanningPlans(), volunteer.getPlanningPlans());
        removePlans(volunteer, volunteerToRemove, planningToRemove);


        var user = keycloakUserService.getUserById(volunteer.getId());
        userAttributeProvider.invalidateUserCache(userId);
        return UserAssemblingMapper.toUserEventDto(volunteer, user);
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

    private Set<Long> toAdd(Collection<String> request, Collection<ShiftPlan> existingPlans) {
        var existingIds = existingPlans.stream().map(ShiftPlan::getId).collect(Collectors.toSet());
        var requestSet = request.stream().map(ConvertUtil::idToLong).collect(Collectors.toSet());
        requestSet.removeAll(existingIds);
        return requestSet;
    }

    private Set<Long> toRemove(Collection<String> request, Collection<ShiftPlan> existingPlans) {
        var existingIds = existingPlans.stream().map(ShiftPlan::getId).collect(Collectors.toSet());
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

    private static <T> Collection<T> initIfNull(Collection<T> set) {
        return set != null ? set : new HashSet<>();
    }
}
