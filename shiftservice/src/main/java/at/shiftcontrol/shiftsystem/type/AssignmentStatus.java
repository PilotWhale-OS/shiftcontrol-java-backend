package at.shiftcontrol.shiftsystem.type;

public enum AssignmentStatus {
    /**
     *  The assignment has been accepted by the user, or he has assigned themselves to it.
     */
    ACCEPTED,
    /**
     *  The assignment is pending, either manually assigned or auto assigned.
     *  => The user has not accepted the assignment yet
     */
    PENDING_MANUAL_ASSIGNED,
    PENDING_AUTO_ASSIGNED,

    /**
     *  The assignment is in the auction state, waiting for others to take it.
     */
    AUCTION,

    /**
     *  The assignment is in the auction state, but has an open swap request.
     */
    AUCTION_SWAP_PENDING,

    /**
     *  The assignment has an open swap request. Waiting for the other user to accept the swap.
     */
    SWAP_PENDING;
}
