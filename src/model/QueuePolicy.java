package model;

/** Scheduling policy used inside one level of MLQ / MLFQ. */
public enum QueuePolicy {
    FCFS("FCFS"), ROUND_ROBIN("Round Robin"), PRIORITY("Priority");

    private final String label;

    QueuePolicy(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
