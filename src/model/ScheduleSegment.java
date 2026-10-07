package model;

/** One block of the Gantt chart: [start, end) on the CPU timeline. */
public class ScheduleSegment {
    private final SegmentType type;
    private final String pid;
    private final int start;
    private int end;

    public ScheduleSegment(SegmentType type, String pid, int start, int end) {
        this.type = type;
        this.pid = pid;
        this.start = start;
        this.end = end;
    }

    public SegmentType getType() { return type; }
    public String getPid() { return pid; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public void setEnd(int end) { this.end = end; }
    public int getLength() { return end - start; }

    /** P1, IDLE or CS. */
    public String getLabel() {
        switch (type) {
            case IDLE: return "IDLE";
            case CONTEXT_SWITCH: return "CS";
            default: return pid;
        }
    }

    @Override
    public String toString() {
        return getLabel() + ":" + start + "-" + end;
    }
}
