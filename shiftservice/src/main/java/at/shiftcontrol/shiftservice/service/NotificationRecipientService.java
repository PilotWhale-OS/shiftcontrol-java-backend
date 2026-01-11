package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsDto;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsFilterDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;

public interface NotificationRecipientService {
    @AdminOnly
    AccountInfoDto getRecipientInformation(String recipientid);

    RecipientsDto getRecipientsForNotification(RecipientsFilterDto filter);
}
