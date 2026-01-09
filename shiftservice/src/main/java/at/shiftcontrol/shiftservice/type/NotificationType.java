package at.shiftcontrol.shiftservice.type;

public enum NotificationType {

    /* volunteer notifications */
    VOLUNTEER_AUTO_ASSIGNED,
    VOLUNTEER_TRADE_REQUESTED,
    VOLUNTEER_TRADES_AUCTIONS_REQUESTS_CHANGED,
    VOLUNTEER_SHIFT_REMINDER,

    /* planner notifications */
    PLANNER_VOLUNTEER_JOINED_PLAN,
    PLANNER_VOLUNTEER_REQUESTED_ACTION,
    PLANNER_TRUST_ALERT_TRIGGERED,

    /* admin notifications */
    ADMIN_PLANNER_JOINED_PLAN,
    ADMIN_REWARD_SYNC_USED
}
