package scheduler;

import java.util.Comparator;

import model.Process;
import model.SimulationConfig;

/**
 * Shortest Job First (non-preemptive).
 * Key: (burst time, arrival time, PID). Min-heap: insert/extract O(log n) -> O(n log n) total.
 */
public class SJFScheduler extends AbstractHeapScheduler {
    private static final Comparator<Process> ORDER = (a, b) -> {
        if (a.getBurstTime() != b.getBurstTime()) {
            return Integer.compare(a.getBurstTime(), b.getBurstTime());
        }
        if (a.getArrivalTime() != b.getArrivalTime()) {
            return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
        }
        return Process.comparePid(a.getPid(), b.getPid());
    };

    public SJFScheduler(SimulationConfig config) {
        super("SJF", config, ORDER);
    }

    @Override
    public String getDataStructureDescription() {
        return "MyPriorityQueue - min-heap keyed by (burst, arrival, PID)";
    }
}
