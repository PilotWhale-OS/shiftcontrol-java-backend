package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.keycloak.representations.idm.AbstractUserRepresentation;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.NotificationSettingsDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsDto;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsFilterDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.mapper.AccountInfoMapper;
import at.shiftcontrol.shiftservice.service.NotificationRecipientService;
import at.shiftcontrol.shiftservice.type.ReceiverAccessLevel;

@Service
@RequiredArgsConstructor
public class NotificationRecipientServiceImpl implements NotificationRecipientService {
    private final VolunteerDao volunteerDao;
    private final KeycloakUserService keycloakUserService;
    private final NotificationSettingsDao notificationSettingsDao;

    @AdminOnly
    @Override
    public AccountInfoDto getRecipientInformation(String recipientid) {
        var user = keycloakUserService.getUserById(recipientid);
        return AccountInfoMapper.toDto(user);
    }

    @Override
    @AdminOnly
    public RecipientsDto getRecipientsForNotification(RecipientsFilterDto filter) {
        validateFilter(filter);
        // filter from cheapest and most narrowing query first
        Collection<String> recipientIds = null;
        // if admin access level, all other filters are automatically true: in all plans and events
        if (filter.getReceiverAccessLevel() == ReceiverAccessLevel.ADMIN) {
            recipientIds = filterAdminsById(filter, recipientIds);
        } else {
            // filter by eventId, shiftId and volunteerIds if present
            recipientIds = filterByShiftPlanAndVolunteerIds(filter, recipientIds);
            recipientIds = filterByEventAndVolunteerIds(filter, recipientIds);
            recipientIds = filterByVolunteerIds(filter, recipientIds);
        }

        if (recipientIds != null) {
            if (filter.getNotificationChannel() == NotificationChannel.EMAIL) {
                // EMAIL default OFF --> include only explicit settings
                recipientIds = notificationSettingsDao.findAllByNotificationTypeAndChannelAndUserIds(
                        filter.getNotificationType(),
                        NotificationChannel.EMAIL,
                        recipientIds
                    );
            } else {
                // other channels are default ON --> remove those who have the type and channel DISABLED
                Collection<String> nonRecipients = notificationSettingsDao.findAllByNotificationTypeAndChannelDisabled(
                    filter.getNotificationType(),
                    filter.getNotificationChannel(),
                    recipientIds);
                recipientIds.removeAll(nonRecipients);
            }
        } else {
            // else get all that have the notification type and channel enabled
                // this should never be called imo, since all planner & volunteer events always should have a filter enabled
                // only admin notifications should have no filter, but that's handled at the top
            recipientIds = notificationSettingsDao.findAllByNotificationTypeAndChannel(
                filter.getNotificationType(),
                filter.getNotificationChannel()
            );
        }

        // get users and build response
        if (recipientIds.isEmpty()) {
            return RecipientsDto.builder().recipients(Set.of()).build();
        } else {
            var users = keycloakUserService.getUserByIds(recipientIds);
            var recipients = users.stream().map(AccountInfoMapper::toDto).collect(Collectors.toSet());
            return RecipientsDto.builder()
                .recipients(recipients)
                .build();
        }
    }

    private Collection<String> filterByEventAndVolunteerIds(RecipientsFilterDto filter, Collection<String> recipientIds) {
        if (filter.getRelatedEventId() == null) {
            return recipientIds;
        }
        if (filter.getRelatedVolunteerIds() != null) {
            return (switch (filter.getReceiverAccessLevel()) {
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
        } else {
            return (switch (filter.getReceiverAccessLevel()) {
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

    private Collection<String> filterByShiftPlanAndVolunteerIds(RecipientsFilterDto filter, Collection<String> recipientIds) {
        if (filter.getRelatedShiftPlanId() == null) {
            return recipientIds;
        }
        if (filter.getRelatedVolunteerIds() != null) {
            return (switch (filter.getReceiverAccessLevel()) {
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
        } else {
            return (switch (filter.getReceiverAccessLevel()) {
                case VOLUNTEER -> volunteerDao.findAllByShiftPlan(
                    ConvertUtil.idToLong(filter.getRelatedShiftPlanId())
                );
                case PLANNER -> volunteerDao.findAllPlannersByShiftPlan(
                    ConvertUtil.idToLong(filter.getRelatedShiftPlanId())
                );
                case ADMIN -> throw new NotImplementedException(); // should be handled at root
            }).stream().map(Volunteer::getId).collect(Collectors.toSet());
        }
    }

    private Collection<String> filterByVolunteerIds(RecipientsFilterDto filter, Collection<String> recipientIds) {
        if (filter.getRelatedVolunteerIds() != null) {
            return (switch (filter.getReceiverAccessLevel()) {
                case VOLUNTEER -> volunteerDao.findAllByVolunteerIds(
                    filter.getRelatedVolunteerIds()
                );
                case PLANNER -> volunteerDao.findAllByPlannerIds(
                    filter.getRelatedVolunteerIds()
                );
                case ADMIN -> throw new NotImplementedException(); // should be handled at root
            }).stream().map(Volunteer::getId).collect(Collectors.toSet());
        } else {
            return recipientIds;
        }
    }

    private Collection<String> filterAdminsById(RecipientsFilterDto filter, Collection<String> recipientIds) {
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
        return recipientIds;
    }

    private void validateFilter(RecipientsFilterDto filter) {
        if (filter.getRelatedEventId() != null && filter.getRelatedShiftPlanId() != null) {
            throw new BadRequestException("Recipients can be filtered either by associated event or shift plan.");
        }
    }
}
