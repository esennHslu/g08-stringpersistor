package ch.hslu.vsk.stringpersistor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class InstantHelper {
    private InstantHelper() {
        throw new AssertionError();
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")
            .withZone(ZoneOffset.UTC);

    public static String format(final Instant instant) {
        return FORMATTER.format(instant);
    }

    public static Instant parse(final String time) {
        var localDateTime = LocalDateTime.parse(time, FORMATTER);
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
