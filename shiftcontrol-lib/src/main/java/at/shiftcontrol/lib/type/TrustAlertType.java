package at.shiftcontrol.lib.type;

public enum TrustAlertType {

    SPAM,       // multiple joins and leaves for the same user and slot within a specific time
    OVERLOAD,   // several signups for a user to different slots within a specific time
    TRADE,      // many open trade requests for a specific user
    AUCTION     // many open auctions for a specific user
}
