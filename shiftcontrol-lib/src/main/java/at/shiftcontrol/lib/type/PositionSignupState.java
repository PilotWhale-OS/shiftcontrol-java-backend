package at.shiftcontrol.lib.type;

import lombok.Getter;

public enum PositionSignupState {
    SIGNED_UP("Already signed up in slot"),
    SIGNUP_POSSIBLE("A normal signup is possible"),
    SIGNUP_VIA_TRADE("This slot can be signed up only via a trade"),
    SIGNUP_VIA_AUCTION("This slot can be signed up only via an auction"),
    FULL("Slot already is full"),
    NOT_ELIGIBLE("The required role for this slot is missing"),
    SIGNUP_OR_TRADE("A normal signup is possible");


    @Getter
    private final String message;

    PositionSignupState(String message) {
        this.message = message;
    }
}
