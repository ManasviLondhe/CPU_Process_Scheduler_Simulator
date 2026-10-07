package model;

/** The algorithms offered by the simulator. A new algorithm = new constant + SchedulerFactory registration. */
public enum SchedulingAlgorithmType {
    FCFS("FCFS", "First Come First Serve - non-preemptive, ordered by arrival (Queue)."),
    SJF("SJF", "Shortest Job First - non-preemptive, smallest burst first (Min-Heap)."),
    SRTF("SRTF", "Shortest Remaining Time First - preemptive SJF (Min-Heap by remaining time)."),
    PRIORITY("Priority", "Preemptive priority scheduling with optional aging (Min-Heap)."),
    ROUND_ROBIN("Round Robin", "Time-sliced rotation with a configurable quantum (Circular Queue)."),
    MLQ("Multilevel Queue", "Fixed queues per process class, each with its own policy."),
    MLFQ("Multilevel Feedback Queue", "Processes move between levels (demotion / aging promotion).");

    private final String displayName;
    private final String description;

    SchedulingAlgorithmType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
