package at.shiftcontrol.lib.util;

import at.shiftcontrol.lib.exception.BadRequestException;

public class ConvertUtil {
    public static long idToLong(String value) throws BadRequestException {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException("id is null or empty");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("id is not a valid long: " + value);
        }
    }
}
