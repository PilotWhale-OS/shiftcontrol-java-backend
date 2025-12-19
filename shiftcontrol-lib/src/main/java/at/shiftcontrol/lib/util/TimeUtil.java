package at.shiftcontrol.lib.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtil {
    private static final ZoneId VIENNA_ZONE_ID = ZoneId.of("Europe/Vienna");
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    /**
     * Returns the current date and time in Vienna converted to UTC.
     *
     * @return The current date and time in Vienna converted to UTC as a {@link Instant} object.
     */
    public static Instant getCurrentViennaTimeInUtc() {
        return ZonedDateTime.now(VIENNA_ZONE_ID).withZoneSameInstant(UTC_ZONE_ID).toInstant();
    }

    /**
     * Creates a {@link Instant} object with the given date and time in Vienna and converts it to UTC.
     *
     * @return The {@link Instant} object with the given date and time in Vienna converted to UTC.
     */
    public static Instant createViennaTimeInUtc(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, VIENNA_ZONE_ID).withZoneSameInstant(UTC_ZONE_ID).toInstant();
    }

    public static LocalDate convertToUtcLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, UTC_ZONE_ID).toLocalDate();
    }

    public static Instant convertToStartOfUtcDayInstant(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.atStartOfDay(UTC_ZONE_ID).toInstant();
    }

    public static Instant convertToEndOfUtcDayInstant(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.atTime(23, 59, 59, 999_999_999).atZone(UTC_ZONE_ID).toInstant();
    }

    public static long calculateDurationInMinutes(Instant startInstant, Instant endInstant) {
        if (startInstant == null || endInstant == null) {
            return 0;
        }
        long durationInMillis = endInstant.toEpochMilli() - startInstant.toEpochMilli();
        return durationInMillis / (1000 * 60);
    }

    /**
     * Formats the given {@link Instant} object to a string with the pattern "dd.MM.yyyy HH:mm:ss.SSS".
     *
     * @param instant The {@link Instant} object to format.
     * @return The formatted string.
     */
    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");
        return instant.atZone(UTC_ZONE_ID).format(formatter);
    }
}
