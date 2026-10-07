package model;

import java.util.ArrayList;
import java.util.List;

/** All user-adjustable parameters of one simulation run. */
public class SimulationConfig {
    private SchedulingAlgorithmType algorithm = SchedulingAlgorithmType.FCFS;
    private int timeQuantum = 2;
    private boolean priorityLowerIsHigher = true;
    private boolean agingEnabled = false;
    private int agingInterval = 5;
    private boolean contextSwitchEnabled = false;
    private int contextSwitchCost = 1;
    private int starvationThreshold = 30;
    private List<QueueLevelConfig> mlqLevels = defaultMlqLevels();
    private List<QueueLevelConfig> mlfqLevels = defaultMlfqLevels();

    public static List<QueueLevelConfig> defaultMlqLevels() {
        List<QueueLevelConfig> l = new ArrayList<>();
        l.add(new QueueLevelConfig("System", QueuePolicy.PRIORITY, 2));
        l.add(new QueueLevelConfig("Interactive", QueuePolicy.ROUND_ROBIN, 4));
        l.add(new QueueLevelConfig("Background", QueuePolicy.FCFS, 2));
        return l;
    }

    public static List<QueueLevelConfig> defaultMlfqLevels() {
        List<QueueLevelConfig> l = new ArrayList<>();
        l.add(new QueueLevelConfig("Level 1", QueuePolicy.ROUND_ROBIN, 2));
        l.add(new QueueLevelConfig("Level 2", QueuePolicy.ROUND_ROBIN, 4));
        l.add(new QueueLevelConfig("Level 3", QueuePolicy.FCFS, 2));
        return l;
    }

    public SimulationConfig copy() {
        SimulationConfig c = new SimulationConfig();
        c.algorithm = algorithm;
        c.timeQuantum = timeQuantum;
        c.priorityLowerIsHigher = priorityLowerIsHigher;
        c.agingEnabled = agingEnabled;
        c.agingInterval = agingInterval;
        c.contextSwitchEnabled = contextSwitchEnabled;
        c.contextSwitchCost = contextSwitchCost;
        c.starvationThreshold = starvationThreshold;
        c.mlqLevels = copyLevels(mlqLevels);
        c.mlfqLevels = copyLevels(mlfqLevels);
        return c;
    }

    private static List<QueueLevelConfig> copyLevels(List<QueueLevelConfig> src) {
        List<QueueLevelConfig> out = new ArrayList<>();
        for (QueueLevelConfig q : src) {
            out.add(q.copy());
        }
        return out;
    }

    /** Short text description used in exports and the history page. */
    public String describe() {
        StringBuilder sb = new StringBuilder(algorithm.getDisplayName());
        switch (algorithm) {
            case ROUND_ROBIN:
                sb.append(" q=").append(timeQuantum);
                break;
            case PRIORITY:
                sb.append(priorityLowerIsHigher ? " (lower number = higher priority)" : " (higher number = higher priority)");
                if (agingEnabled) {
                    sb.append(" aging every ").append(agingInterval);
                }
                break;
            case MLQ:
                sb.append(" ").append(mlqLevels);
                break;
            case MLFQ:
                sb.append(" ").append(mlfqLevels);
                if (agingEnabled) {
                    sb.append(" aging threshold ").append(agingInterval);
                }
                break;
            default:
                break;
        }
        sb.append(contextSwitchEnabled ? ", context switch cost " + contextSwitchCost : ", no context switch cost");
        return sb.toString();
    }

    public SchedulingAlgorithmType getAlgorithm() { return algorithm; }
    public void setAlgorithm(SchedulingAlgorithmType algorithm) { this.algorithm = algorithm; }
    public int getTimeQuantum() { return timeQuantum; }
    public void setTimeQuantum(int timeQuantum) { this.timeQuantum = timeQuantum; }
    public boolean isPriorityLowerIsHigher() { return priorityLowerIsHigher; }
    public void setPriorityLowerIsHigher(boolean v) { this.priorityLowerIsHigher = v; }
    public boolean isAgingEnabled() { return agingEnabled; }
    public void setAgingEnabled(boolean agingEnabled) { this.agingEnabled = agingEnabled; }
    public int getAgingInterval() { return agingInterval; }
    public void setAgingInterval(int agingInterval) { this.agingInterval = agingInterval; }
    public boolean isContextSwitchEnabled() { return contextSwitchEnabled; }
    public void setContextSwitchEnabled(boolean v) { this.contextSwitchEnabled = v; }
    public int getContextSwitchCost() { return contextSwitchCost; }
    public void setContextSwitchCost(int cost) { this.contextSwitchCost = cost; }
    public int getStarvationThreshold() { return starvationThreshold; }
    public void setStarvationThreshold(int t) { this.starvationThreshold = t; }
    public List<QueueLevelConfig> getMlqLevels() { return mlqLevels; }
    public void setMlqLevels(List<QueueLevelConfig> levels) { this.mlqLevels = levels; }
    public List<QueueLevelConfig> getMlfqLevels() { return mlfqLevels; }
    public void setMlfqLevels(List<QueueLevelConfig> levels) { this.mlfqLevels = levels; }
}
