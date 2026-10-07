package util;

/** Application-wide limits and names. */
public final class Constants {
    public static final String APP_NAME = "CPU Process Scheduler Simulator";
    public static final int MAX_PROCESSES = 200;
    public static final int MAX_BURST_TIME = 100_000;
    public static final int MAX_ARRIVAL_TIME = 1_000_000;
    public static final int MAX_PRIORITY = 1000;
    public static final int MAX_QUEUE_LEVELS = 5;
    public static final int MAX_PID_LENGTH = 12;
    public static final int MAX_NAME_LENGTH = 30;
    public static final int BASE_TICK_MILLIS = 600;

    private Constants() {
    }
}
