package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsDto;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsFilterDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.repo.userprofile.NotificationRepository;
import at.shiftcontrol.shiftservice.service.NotificationRecipientService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationRecipientServiceImpl implements NotificationRecipientService {

    private final NotificationRepository notificationRepository;
    private final VolunteerDao volunteerDao;
    private final KeycloakUserService keycloakUserService;

    @AdminOnly
    @Override
    public AccountInfoDto getRecipientInformation(String recipientid) {
        var volunteerOpt = volunteerDao.findById(recipientid);
        if(volunteerOpt.isEmpty()){
            throw new BadRequestException("No volunteer found with id: " + recipientid);
        }
        var volunteer = volunteerOpt.get();
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

        // filter from cheapest and most narrowing query first
        Collection<Volunteer> recipients = null;

        // if specific volunteers are requested, only query those
        if(filter.getRelatedVolunteerIds() != null) {
            if(filter.getRelatedShiftPlanId() != null){
                recipients = volunteerDao.findAllByShiftPlanAndVolunteerIds(
                    ConvertUtil.idToLong(filter.getRelatedShiftPlanId()),
                    filter.getRelatedVolunteerIds()
                );
            } else if(filter.getRelatedEventId() != null) {
                recipients = volunteerDao.findAllByEventAndVolunteerIds(
                    ConvertUtil.idToLong(filter.getRelatedEventId()),
                    filter.getRelatedVolunteerIds()
                );
            } else {
                recipients = volunteerDao.findAllByVolunteerIds(
                    filter.getRelatedVolunteerIds()
                );
            }
        }

        // query all volunteers by event or shiftplan
        else {
            if(filter.getRelatedShiftPlanId() != null){
                recipients = volunteerDao.findAllByShiftPlan(
                    ConvertUtil.idToLong(filter.getRelatedShiftPlanId())
                );
            } else if(filter.getRelatedEventId() != null) {
                recipients = volunteerDao.findAllByEvent(
                    ConvertUtil.idToLong(filter.getRelatedEventId())
                );
            }
        }

        if(recipients != null) {
            // if preselection made: filter by notification type and channel
            recipients = notificationRepository.findAllByVolunteerIdAndNotificationTypeAndChannelEnabled(
                filter.getNotificationType(),
                filter.getNotificationChannel(),
                recipients.stream().map(Volunteer::getId).collect(Collectors.toList())
            );
        } else {
            // else get all that have the notification type and channel enabled
            recipients = notificationRepository.findAllByNotificationTypeAndChannelEnabled(
                filter.getNotificationType(),
                filter.getNotificationChannel()
            );
        }

        // TODO: check for access level as specified in filter

        /* no recipients; just return empty  */
        if(recipients.isEmpty()){
            return RecipientsDto.builder().recipients(Set.of()).build();
        }

        /* only one recipient found; get by id from keycloak  */
        else if(recipients.size() == 1) {
            var recipient = recipients.iterator().next();
            var user = keycloakUserService.getUserById(recipient.getId());
            var accountInfo = AccountInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fistName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(UserType.ASSIGNED) // TODO: determine user type properly? not really relevant here. could also use a slim DTO
                .build();

            return RecipientsDto.builder()
                .recipients(Set.of(accountInfo))
                .build();
        }

        /* many recipients found: getting all keycloak users and then filtering is probably more performant than doing many single calls */
        else {
            var users = keycloakUserService.getAllUsers();
            Collection<Volunteer> finalRecipients = recipients;
            var relevantUsers = users.stream()
                .filter(u -> finalRecipients.stream().anyMatch(r -> r.getId().equals(u.getId())))
                .collect(Collectors.toSet());

            var recipientDtos = relevantUsers.stream().map(user -> AccountInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fistName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(UserType.ASSIGNED) // TODO: determine user type properly? not really relevant here. could also use a slim DTO
                .build()).collect(Collectors.toSet());

            return RecipientsDto.builder()
                .recipients(recipientDtos)
                .build();
        }
    }

    private void validateFilter(RecipientsFilterDto filter) {
        if(filter.getRelatedEventId() != null && filter.getRelatedShiftPlanId() != null) {
            throw new BadRequestException("Recipients can be filtered either by associated event or shift plan.");
        }
    }
}
