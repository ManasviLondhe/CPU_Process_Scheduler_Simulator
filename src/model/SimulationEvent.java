package model;

/** One entry of the event log. */
public class SimulationEvent {
    private final int time;
    private final SimulationEventType type;
    private final String pid;
    private final String message;

    public SimulationEvent(int time, SimulationEventType type, String pid, String message) {
        this.time = time;
        this.type = type;
        this.pid = pid;
        this.message = message;
    }

    public int getTime() { return time; }
    public SimulationEventType getType() { return type; }
    public String getPid() { return pid; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("[t=%3d] %-18s %s", time, type, message);
    }
}
