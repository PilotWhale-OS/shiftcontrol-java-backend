package at.shiftcontrol.lib.util;

import java.util.List;
import java.util.stream.Stream;

import at.shiftcontrol.lib.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvertUtil {
    public static long idToLong(String value) throws BadRequestException {
        if (value == null || value.isEmpty()) {
            log.error("Id is not present.");
            throw new BadRequestException("Something unexpected happened while processing the request.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("Id is not valid: {}", value);
            // return user friendly message without mentioning exception details like id
            throw new BadRequestException("Something unexpected happened while processing the request.");
        }
    }

    public static List<String> toStringList(List<Long> ids) {
        return toStringList(ids.stream());
    }

    public static List<String> toStringList(Stream<Long> stream) {
        return stream.map(String::valueOf).toList();
    }
}
