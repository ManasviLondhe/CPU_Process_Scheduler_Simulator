package generator;

/** Ready-made parameter sets for the random workload generator. */
public enum WorkloadPreset {
    LIGHT("Light", 5, 10, 1, 6, 5, false),
    MEDIUM("Medium", 10, 20, 2, 12, 10, false),
    HEAVY("Heavy", 25, 40, 3, 20, 10, false),
    CPU_BOUND("CPU-bound", 8, 5, 15, 40, 10, false),
    MIXED("Mixed (short + long)", 12, 25, 1, 30, 10, true);

    private final String label;
    private final int count;
    private final int maxArrival;
    private final int minBurst;
    private final int maxBurst;
    private final int maxPriority;
    private final boolean mixed;

    WorkloadPreset(String label, int count, int maxArrival, int minBurst, int maxBurst, int maxPriority, boolean mixed) {
        this.label = label;
        this.count = count;
        this.maxArrival = maxArrival;
        this.minBurst = minBurst;
        this.maxBurst = maxBurst;
        this.maxPriority = maxPriority;
        this.mixed = mixed;
    }

    public int getCount() { return count; }
    public int getMaxArrival() { return maxArrival; }
    public int getMinBurst() { return minBurst; }
    public int getMaxBurst() { return maxBurst; }
    public int getMaxPriority() { return maxPriority; }
    public boolean isMixed() { return mixed; }

    @Override
    public String toString() {
        return label;
    }
}
