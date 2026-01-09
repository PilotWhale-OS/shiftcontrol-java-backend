package at.shiftcontrol.shiftservice.repo.userprofile;

import java.util.Collection;
import java.util.List;

import at.shiftcontrol.shiftservice.entity.Volunteer;

import at.shiftcontrol.shiftservice.type.NotificationChannel;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignmentId;
import at.shiftcontrol.shiftservice.type.NotificationType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<VolunteerNotificationAssignment, VolunteerNotificationAssignmentId> {
    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerId(String volunteerId);

    @Query("""
    select v
    from Volunteer v
    where v.id in (
        select vna.volunteerNotificationAssignmentId.volunteerId
        from VolunteerNotificationAssignment vna
        where vna.volunteerNotificationAssignmentId.notificationType = :notificationType
          and vna.volunteerNotificationAssignmentId.notificationChannel = :notificationChannel
    )
""")
    List<Volunteer> findAllByNotificationTypeAndChannelEnabled(
        @Param("notificationType") NotificationType notificationType,
        @Param("notificationChannel") NotificationChannel notificationChannel
    );


    @Query("""
    select v
    from Volunteer v
    where v.id in :volunteerIds
      and v.id in (
        select vna.volunteerNotificationAssignmentId.volunteerId
        from VolunteerNotificationAssignment vna
        where vna.volunteerNotificationAssignmentId.notificationType = :notificationType
          and vna.volunteerNotificationAssignmentId.notificationChannel = :notificationChannel
    )
""")
    List<Volunteer> findAllByVolunteerIdAndNotificationTypeAndChannelEnabled(
        @Param("notificationType") NotificationType notificationType,
        @Param("notificationChannel") NotificationChannel notificationChannel,
        @Param("volunteerIds") Collection<String> volunteerIds
    );

    List<VolunteerNotificationAssignment> findAllByVolunteerNotificationAssignmentId_VolunteerIdAndVolunteerNotificationAssignmentId_NotificationType(
        String volunteerId,
        NotificationType notificationType
    );
}
