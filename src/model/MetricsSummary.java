package model;

/** Aggregated performance numbers of a simulation (see MetricsCalculator). */
public class MetricsSummary {
    private final int completedCount;
    private final double avgWaitingTime;
    private final double avgTurnaroundTime;
    private final double avgResponseTime;
    private final double cpuUtilization;
    private final double throughput;
    private final int totalTime;
    private final int busyTime;
    private final int idleTime;
    private final int contextSwitches;
    private final int contextSwitchTime;
    private final int preemptions;

    public MetricsSummary(int completedCount, double avgWaitingTime, double avgTurnaroundTime,
                          double avgResponseTime, double cpuUtilization, double throughput,
                          int totalTime, int busyTime, int idleTime,
                          int contextSwitches, int contextSwitchTime, int preemptions) {
        this.completedCount = completedCount;
        this.avgWaitingTime = avgWaitingTime;
        this.avgTurnaroundTime = avgTurnaroundTime;
        this.avgResponseTime = avgResponseTime;
        this.cpuUtilization = cpuUtilization;
        this.throughput = throughput;
        this.totalTime = totalTime;
        this.busyTime = busyTime;
        this.idleTime = idleTime;
        this.contextSwitches = contextSwitches;
        this.contextSwitchTime = contextSwitchTime;
        this.preemptions = preemptions;
    }

    public int getCompletedCount() { return completedCount; }
    public double getAvgWaitingTime() { return avgWaitingTime; }
    public double getAvgTurnaroundTime() { return avgTurnaroundTime; }
    public double getAvgResponseTime() { return avgResponseTime; }
    /** Percentage 0..100: useful process execution time / total time. */
    public double getCpuUtilization() { return cpuUtilization; }
    /** Completed processes per time unit. */
    public double getThroughput() { return throughput; }
    public int getTotalTime() { return totalTime; }
    public int getBusyTime() { return busyTime; }
    public int getIdleTime() { return idleTime; }
    public int getContextSwitches() { return contextSwitches; }
    public int getContextSwitchTime() { return contextSwitchTime; }
    public int getPreemptions() { return preemptions; }
}
