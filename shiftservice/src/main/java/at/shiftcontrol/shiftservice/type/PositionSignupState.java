package at.shiftcontrol.shiftservice.type;

public enum PositionSignupState {
    SIGNED_UP,         // current user is already assigned to this slot
    SIGNUP_POSSIBLE,   // current user can join this slot (free & eligible)
    FULL,              // slot has no capacity left
    LOCKED,            // slot or shift cannot be self-assigned
    NOT_ELIGIBLE       // user cannot join (wrong role/qualification)
}

