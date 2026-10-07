package simulation;

/** Counts context switches and the CPU time they consumed; knows whether a switch costs time. */
public class ContextSwitchManager {
    private final boolean enabled;
    private final int cost;
    private int switchCount;
    private int totalTime;

    public ContextSwitchManager(boolean enabled, int cost) {
        this.enabled = enabled;
        this.cost = Math.max(0, cost);
    }

    /** True when a switch actually occupies the CPU for one or more time units. */
    public boolean isCharged() {
        return enabled && cost > 0;
    }

    public int getCost() {
        return cost;
    }

    public void recordSwitch() {
        switchCount++;
    }

    public void addTime(int units) {
        totalTime += units;
    }

    public int getSwitchCount() {
        return switchCount;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
