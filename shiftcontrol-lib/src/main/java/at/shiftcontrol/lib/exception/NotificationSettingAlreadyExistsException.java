package at.shiftcontrol.lib.exception;

public class NotificationSettingAlreadyExistsException extends RuntimeException {
    public NotificationSettingAlreadyExistsException(String message) {
        super(message);
    }
}
