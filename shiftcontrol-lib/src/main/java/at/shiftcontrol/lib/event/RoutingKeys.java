package at.shiftcontrol.lib.event;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

public final class RoutingKeys {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)}");

    public static final String ACTIVITY_CREATED = "activity.created";
    public static final String ACTIVITY_UPDATED = "activity.updated.{activityId}";
    public static final String ACTIVITY_DELETED = "activity.deleted.{activityId}";

    public static final String TRADE_REQUEST_CREATED = "trade.request.created.{requestedVolunteerId}.{offeringVolunteerId}";
    public static final String TRADE_REQUEST_DECLINED = "trade.request.declined.{requestedVolunteerId}.{offeringVolunteerId}";
    public static final String TRADE_REQUEST_CANCELED = "trade.request.canceled.{requestedVolunteerId}.{offeringVolunteerId}";

    public static final String EVENT_CREATED = "event.created";
    public static final String EVENT_CLONED = "event.cloned.{sourceEventId}.{newEventId}";
    public static final String EVENT_UPDATED = "event.updated.{eventId}";
    public static final String EVENT_DELETED = "event.deleted.{eventId}";
    public static final String EVENT_EXPORTED = "event.exported.{eventId}.{exportFormat}";

    public static final String LOCATION_CREATED = "location.created";
    public static final String LOCATION_UPDATED = "location.updated.{locationId}";
    public static final String LOCATION_DELETED = "location.deleted.{locationId}";

    public static final String POSITIONSLOT_CREATED = "positionslot.created";
    public static final String POSITIONSLOT_UPDATED = "positionslot.updated.{positionSlotId}";
    public static final String POSITIONSLOT_DELETED = "positionslot.deleted.{positionSlotId}";

    public static final String POSITIONSLOT_JOINED = "positionslot.joined.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_LEFT = "positionslot.left.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_REQUEST_JOIN = "positionslot.request.join.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_REQUEST_LEAVE = "positionslot.request.leave.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_PREFERENCE_UPDATED = "positionslot.preference.updated.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_REQUEST_ACCEPTED = "positionslot.request.accepted.{volunteerId}.{positionSlotId}";
    public static final String POSITIONSLOT_REQUEST_DECLINED = "positionslot.request.declined.{volunteerId}.{positionSlotId}";

    public static final String AUCTION_CREATED = "auction.created.{positionSlotId}";
    public static final String AUCTION_CLAIMED = "auction.claimed.{positionSlotId}.{volunteerId}";
    public static final String AUCTION_CANCELED = "auction.canceled.{positionSlotId}";

    public static final String ASSIGNMENT_SWITCH_COMPLETED = "assignment.switch.completed.{requestedVolunteerId}.{offeringVolunteerId}";

    public static final String SHIFTPLAN_CREATED = "shiftplan.created";
    public static final String SHIFTPLAN_UPDATED = "shiftplan.updated.{shiftPlanId}";
    public static final String SHIFTPLAN_DELETED = "shiftplan.deleted.{shiftPlanId}";
    public static final String SHIFTPLAN_INVITE_CREATED = "shiftplan.invite.created.{shiftPlanId}.{inviteId}";
    public static final String SHIFTPLAN_INVITE_REVOKED = "shiftplan.invite.revoked.{shiftPlanId}.{inviteId}";
    public static final String SHIFTPLAN_INVITE_DELETED = "shiftplan.invite.deleted.{shiftPlanId}.{inviteId}";
    public static final String SHIFTPLAN_JOINED_VOLUNTEER = "shiftplan.joined.volunteer.{shiftPlanId}.{volunteerId}";
    public static final String SHIFTPLAN_JOINED_PLANNER = "shiftplan.joined.planner.{shiftPlanId}.{volunteerId}";
    public static final String SHIFTPLAN_LOCKSTATUS_CHANGED = "shiftplan.lockstatus.changed.{shiftPlanId}";

    public static final String SHIFT_CREATED = "shift.created";
    public static final String SHIFT_UPDATED = "shift.updated.{shiftId}";
    public static final String SHIFT_DELETED = "shift.deleted.{shiftId}";

    public static final String TIMECONSTRAINT_CREATED = "timeconstraint.created.{volunteerId}";
    public static final String TIMECONSTRAINT_DELETED = "timeconstraint.deleted.{volunteerId}.{timeConstraintId}";

    public static final String ROLE_CREATED = "role.created";
    public static final String ROLE_UPDATED = "role.updated.{roleId}";
    public static final String ROLE_DELETED = "role.deleted.{roleId}";
    public static final String ROLE_ASSIGNED = "role.assigned.{roleId}.{volunteerId}";
    public static final String ROLE_UNASSIGNED = "role.unassigned.{roleId}.{volunteerId}";

    public static final String VOLUNTEER_NOTIFICATION_PREFERENCE_UPDATED = "volunteer.notification.preference.updated.{volunteerId}";
    public static final String REWARDPOINTS_TRANSACTION_CREATED = "rewardpoints.transaction.created.{volunteerId}.{transactionId}";
    public static final String REWARDPOINTS_TRANSACTION_FAILED = "rewardpoints.transaction.failed.{volunteerId}";
    public static final String REWARDPOINTS_SHARETOKEN_CREATED = "rewardpoints.sharetoken.created.{shareTokenId}";
    public static final String REWARDPOINTS_SHARETOKEN_DELETED = "rewardpoints.sharetoken.deleted.{shareTokenId}";


    public static @NonNull String format(@NonNull String template, @NonNull Map<String, ?> values) {
        var m = PLACEHOLDER.matcher(template);
        var sb = new StringBuilder(template.length());

        while (m.find()) {
            String key = m.group(1);
            Object value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for {" + key + "} in: " + template);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
