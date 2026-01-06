package at.shiftcontrol.lib.util;

import java.util.List;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;

public class ConvertUtil {
    public static long idToLong(String value) throws BadRequestException {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException("Id is not present.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Id is not valid.");
        }
    }

    public static List<String> toStringList(List<Long> ids) {
        return toStringList(ids.stream());
    }

    public static List<String> toStringList(Stream<Long> stream) {
        return stream.map(String::valueOf).toList();
    }
}
