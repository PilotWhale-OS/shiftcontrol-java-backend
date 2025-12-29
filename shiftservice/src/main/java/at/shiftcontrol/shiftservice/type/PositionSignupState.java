package at.shiftcontrol.shiftservice.type;

public enum PositionSignupState {
    SIGNED_UP,              // current user is already assigned to this slot
    SIGNUP_POSSIBLE,        // free & eligible
    SIGNUP_VIA_TRADE,       // full but trade request targets this user
    SIGNUP_VIA_AUCTION,     // full but auction mechanism exists
    FULL,                   // slot has no capacity left (and not trade or auction exists)
    NOT_ELIGIBLE,           // user cannot join (wrong role/qualification)
    SIGNUP_OR_TRADE         // user can eiter join normally or via trade
    // TODO integrate SIGNUP_OR_TRADE into all current calculations
}

