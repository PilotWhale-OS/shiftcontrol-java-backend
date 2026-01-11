package at.shiftcontrol.shiftservice.repo.userprofile;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.lib.type.NotificationChannel;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.lib.entity.VolunteerNotificationAssignmentId;
import at.shiftcontrol.lib.type.NotificationType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerId(String volunteerId);

    @Query("""
    select vna.volunteerNotificationAssignmentId.volunteerId
    from VolunteerNotificationAssignment vna
    where vna.volunteerNotificationAssignmentId.notificationType = :notificationType
      and vna.volunteerNotificationAssignmentId.notificationChannel = :notificationChannel
""")
    List<String> findAllByNotificationTypeAndChannelEnabled(
        @Param("notificationType") NotificationType notificationType,
        @Param("notificationChannel") NotificationChannel notificationChannel
    );


    @Query("""
    select vna.volunteerNotificationAssignmentId.volunteerId
    from VolunteerNotificationAssignment vna
    where vna.volunteerNotificationAssignmentId.notificationType = :notificationType
      and vna.volunteerNotificationAssignmentId.notificationChannel = :notificationChannel
""")
    List<String> findAllByVolunteerIdAndNotificationTypeAndChannelEnabled(
        @Param("notificationType") NotificationType notificationType,
        @Param("notificationChannel") NotificationChannel notificationChannel,
        @Param("volunteerIds") Collection<String> volunteerIds
    );

    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerIdAndVolunteerNotificationAssignmentId_NotificationType(
        String volunteerId,
        NotificationType notificationType
    );
}
