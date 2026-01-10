package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.UserMapper;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Service
@RequiredArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {
    private final VolunteerDao volunteerDao;
    private final ShiftPlanDao shiftPlanDao;
    private final KeycloakUserService keycloakUserService;
    private final UserAttributeProvider userAttributeProvider;

    @Override
    @AdminOnly
    public Collection<UserEventDto> getAllUsers() {
        var volunteers = volunteerDao.findAll();
        var users = keycloakUserService.getUserById(volunteers.stream().map(Volunteer::getId).toList());
        return UserMapper.toUserEventDto(volunteers, users);
    }

    @Override
    @AdminOnly
    public UserEventDto getUser(String userId) {
        var volunteer = volunteerDao.getById(userId);
        var user = keycloakUserService.getUserById(userId);
        return UserMapper.toUserEventDto(volunteer, user);
    }

    @Override
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
        return UserMapper.toUserEventDto(volunteer, user);
    }

    private void addPlans(Volunteer volunteer, Set<Long> volunteerToAdd, Set<Long> planningToAdd) {
        volunteer.getVolunteeringPlans().addAll(shiftPlanDao.getByIds(volunteerToAdd));
        volunteer.getPlanningPlans().addAll(shiftPlanDao.getByIds(planningToAdd));
    }

    private void removePlans(Volunteer volunteer, Set<Long> volunteerToRemove, Set<Long> planningToRemove) {
        volunteer.getVolunteeringPlans().removeIf(x -> volunteerToRemove.contains(x.getId()));
        volunteer.getPlanningPlans().removeIf(x -> planningToRemove.contains(x.getId()));
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
        return requestSet;
    }

    private void assertPlansChanged(Volunteer volunteer, UserEventUpdateDto updateDto) {
        var currentVolunteeringPlans = volunteer.getVolunteeringPlans().stream().map(x -> String.valueOf(x.getId())).toList();
        var currentPlaningPlans = volunteer.getPlanningPlans().stream().map(x -> String.valueOf(x.getId())).toList();
        if (new HashSet<>(currentVolunteeringPlans).equals(new HashSet<>(updateDto.getVolunteeringPlans()))
            && new HashSet<>(currentPlaningPlans).equals(new HashSet<>(updateDto.getPlanningPlans()))) {
            throw new IllegalArgumentException("Update does not change anything");
        }
    }
}
