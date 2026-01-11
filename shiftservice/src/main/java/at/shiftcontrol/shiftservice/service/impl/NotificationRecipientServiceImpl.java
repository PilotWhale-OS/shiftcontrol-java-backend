package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsDto;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsFilterDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.mapper.AccountInfoMapper;
import at.shiftcontrol.shiftservice.repo.userprofile.NotificationRepository;
import at.shiftcontrol.shiftservice.service.NotificationRecipientService;
import at.shiftcontrol.shiftservice.type.ReceiverAccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.representations.idm.AbstractUserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRecipientServiceImpl implements NotificationRecipientService {

    private final NotificationRepository notificationRepository;
    private final VolunteerDao volunteerDao;
    private final KeycloakUserService keycloakUserService;

    @AdminOnly
    @Override
    public AccountInfoDto getRecipientInformation(String recipientid) {
        var volunteer = volunteerDao.getById(recipientid);
        var user = keycloakUserService.getUserById(volunteer.getId());
        return AccountInfoDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
            .userType(UserType.ASSIGNED) // TODO: determine user type properly? not really relevant here. could also use a slim DTO
            .build();
    }

    @Override
    @AdminOnly
    public RecipientsDto getRecipientsForNotification(RecipientsFilterDto filter) {

        validateFilter(filter);

        // TODO: default notification settings should be handled if no settings are found for a user

        // filter from cheapest and most narrowing query first
        Collection<String> recipientIds = null;

        /* if admin access level, all other filters are automatically true: in all plans and events */
        if (filter.getReceiverAccessLevel() == ReceiverAccessLevel.ADMIN) {
            var admins = keycloakUserService.getAllAdmins();

            if (filter.getRelatedVolunteerIds() != null) {
                recipientIds = admins.stream()
                    .map(AbstractUserRepresentation::getId)
                    .filter(id -> filter.getRelatedVolunteerIds().contains(id))
                    .collect(Collectors.toSet());
            } else {
                recipientIds = admins.stream()
                    .map(AbstractUserRepresentation::getId)
                    .collect(Collectors.toSet());
            }
        }

        // if specific volunteers are requested, only query those
        else if (filter.getRelatedVolunteerIds() != null) {

            /* filter volunteers on plan level */
            if (filter.getRelatedShiftPlanId() != null) {

                /* depending on access type */
                recipientIds = (switch (filter.getReceiverAccessLevel()) {
                    case VOLUNTEER -> volunteerDao.findAllByShiftPlanAndVolunteerIds(
                        ConvertUtil.idToLong(filter.getRelatedShiftPlanId()),
                        filter.getRelatedVolunteerIds()
                    );
                    case PLANNER -> volunteerDao.findAllByShiftPlanAndPlannerIds(
                        ConvertUtil.idToLong(filter.getRelatedShiftPlanId()),
                        filter.getRelatedVolunteerIds()
                    );
                    case ADMIN -> throw new NotImplementedException(); // should be handled at root
                }).stream().map(Volunteer::getId).collect(Collectors.toSet());
            }

            /* filter on event level */
            else if (filter.getRelatedEventId() != null) {

                /* depending on access type */
                recipientIds = (switch (filter.getReceiverAccessLevel()) {
                    case VOLUNTEER -> volunteerDao.findAllByEventAndVolunteerIds(
                        ConvertUtil.idToLong(filter.getRelatedEventId()),
                        filter.getRelatedVolunteerIds()
                    );
                    case PLANNER -> volunteerDao.findAllByEventAndPlannerIds(
                        ConvertUtil.idToLong(filter.getRelatedEventId()),
                        filter.getRelatedVolunteerIds()
                    );
                    case ADMIN -> throw new NotImplementedException(); // should be handled at root
                }).stream().map(Volunteer::getId).collect(Collectors.toSet());
            }

            /* get all that are recognized regardless of association */
            else {
                recipientIds = (switch (filter.getReceiverAccessLevel()) {
                    case VOLUNTEER -> volunteerDao.findAllByVolunteerIds(
                        filter.getRelatedVolunteerIds()
                    );
                    case PLANNER -> volunteerDao.findAllByPlannerIds(
                        filter.getRelatedVolunteerIds()
                    );
                    case ADMIN -> throw new NotImplementedException(); // should be handled at root
                }).stream().map(Volunteer::getId).collect(Collectors.toSet());
            }
        }

        // query all volunteers by event or shiftplan
        else {

            /* filter by shift plan */
            if (filter.getRelatedShiftPlanId() != null) {

                /* depending on access type */
                recipientIds = (switch (filter.getReceiverAccessLevel()) {
                    case VOLUNTEER -> volunteerDao.findAllByShiftPlan(
                        ConvertUtil.idToLong(filter.getRelatedShiftPlanId())
                    );
                    case PLANNER -> volunteerDao.findAllPlannersByShiftPlan(
                        ConvertUtil.idToLong(filter.getRelatedShiftPlanId())
                    );
                    case ADMIN -> throw new NotImplementedException(); // should be handled at root
                }).stream().map(Volunteer::getId).collect(Collectors.toSet());
            }

            /* filter by event */
            else if (filter.getRelatedEventId() != null) {

                /* depending on access level */
                recipientIds = (switch (filter.getReceiverAccessLevel()) {
                    case VOLUNTEER -> volunteerDao.findAllByEvent(
                        ConvertUtil.idToLong(filter.getRelatedEventId())
                    );
                    case PLANNER -> volunteerDao.findAllPlannersByEvent(
                        ConvertUtil.idToLong(filter.getRelatedEventId())
                    );
                    case ADMIN -> throw new NotImplementedException(); // should be handled at root
                }).stream().map(Volunteer::getId).collect(Collectors.toSet());
            }
        }

        if (recipientIds != null) {

            // if preselection made: filter by notification type and channel
            recipientIds = notificationRepository.findAllByVolunteerIdAndNotificationTypeAndChannelEnabled(
                filter.getNotificationType(),
                filter.getNotificationChannel(),
                recipientIds);
        } else {

            // else get all that have the notification type and channel enabled
            recipientIds = notificationRepository.findAllByNotificationTypeAndChannelEnabled(
                filter.getNotificationType(),
                filter.getNotificationChannel()
            );
        }

        /* no recipients; just return empty  */
        if (recipientIds.isEmpty()) {
            return RecipientsDto.builder().recipients(Set.of()).build();
        }

        /* only one recipient found; get by id from keycloak  */
        else if (recipientIds.size() == 1) {
            var recipientId = recipientIds.iterator().next();
            var user = keycloakUserService.getUserById(recipientId);
            var accountInfo = AccountInfoMapper.toDto(user);

            return RecipientsDto.builder()
                .recipients(Set.of(accountInfo))
                .build();
        }

        /* many recipients found: getting all keycloak users and then filtering is probably more performant than doing many single calls */
        else {
            var users = keycloakUserService.getAllUsers();
            var finalRecipientIds = recipientIds;
            var relevantUsers = users.stream()
                .filter(u -> finalRecipientIds.stream().anyMatch(r -> r.equals(u.getId())))
                .collect(Collectors.toSet());

            var recipientDtos = relevantUsers.stream().map(AccountInfoMapper::toDto).collect(Collectors.toSet());

            return RecipientsDto.builder()
                .recipients(recipientDtos)
                .build();
        }
    }

    private void validateFilter(RecipientsFilterDto filter) {
        if (filter.getRelatedEventId() != null && filter.getRelatedShiftPlanId() != null) {
            throw new BadRequestException("Recipients can be filtered either by associated event or shift plan.");
        }
    }
}
