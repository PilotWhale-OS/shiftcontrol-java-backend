package at.shiftcontrol.lib.type;

public enum PositionSignupState {
    SIGNED_UP,                      // current user is already assigned to this slot
    SIGNUP_POSSIBLE,                // free & eligible
    SIGNUP_VIA_TRADE,               // full but trade request targets this user
    SIGNUP_VIA_AUCTION,             // full but auction mechanism exists
    FULL,                           // slot has no capacity left (and not trade or auction exists)
    NOT_ELIGIBLE,                   // user cannot join (wrong role/qualification)
    TIME_CONFLICT_TIME_CONSTRAINT,  // user has a time conflict with a time constraint
    TIME_CONFLICT_ASSIGNMENT,       // user has a time conflict with an existing assignment
    SIGNUP_OR_TRADE                 // user can either join normally or via trade
}

