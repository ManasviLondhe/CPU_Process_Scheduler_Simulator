package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Number and date formatting helpers. */
public final class FormatUtils {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FormatUtils() {
    }

    public static String decimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    public static String percent(double value) {
        return String.format(Locale.US, "%.2f%%", value);
    }

    public static String dateTime(LocalDateTime t) {
        return t.format(DATE_TIME);
    }

    /** Quotes a value for CSV when it contains a comma, quote or line break. */
    public static String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
