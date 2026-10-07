package tests;

import java.util.List;

import model.ScheduleSegment;

/** Minimal assertion helpers (no JUnit needed, so the project has zero dependencies). */
public final class TestUtil {
    private static int passed;
    private static int failed;

    private TestUtil() {
    }

    public static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("  FAIL: " + name);
        }
    }

    public static void assertEquals(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("  FAIL: " + name + "\n        expected: " + expected + "\n        actual:   " + actual);
        }
    }

    public static void assertClose(String name, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.005) {
            passed++;
        } else {
            failed++;
            System.out.println("  FAIL: " + name + " expected " + expected + " but was " + actual);
        }
    }

    public static void expectException(String name, Runnable action, String messagePart) {
        try {
            action.run();
            failed++;
            System.out.println("  FAIL: " + name + " (no exception thrown)");
        } catch (IllegalArgumentException e) {
            check(name + " message", e.getMessage().contains(messagePart));
        }
    }

    /** "P1:0-2 P2:2-4 IDLE:4-8" */
    public static String gantt(List<ScheduleSegment> segments) {
        StringBuilder sb = new StringBuilder();
        for (ScheduleSegment s : segments) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static void section(String title) {
        System.out.println("== " + title);
    }

    public static int getPassed() { return passed; }
    public static int getFailed() { return failed; }
}
