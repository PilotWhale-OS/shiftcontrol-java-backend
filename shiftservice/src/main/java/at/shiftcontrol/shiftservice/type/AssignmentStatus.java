package at.shiftcontrol.shiftservice.type;

public enum AssignmentStatus {
    /**
     * The assignment has been accepted by the user, or he has assigned themselves to it.
     */
    ACCEPTED,
    /**
     * The assignment is in the auction state, waiting for others to take it.
     */
    AUCTION,
    /**
     * The assignment is in the auction state, but the assigned user has requested the shiftplanner to unassign themselves.
     */
    AUCTION_REQUEST_FOR_UNASSIGN,
    REQUEST_FOR_ASSIGNMENT
}
