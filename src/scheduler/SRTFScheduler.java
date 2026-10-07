package scheduler;

import java.util.Comparator;

import model.Process;
import model.SimulationConfig;

/**
 * Shortest Remaining Time First (preemptive SJF).
 * Key: (remaining time, arrival time, PID).
 * A running process is preempted only when a waiting process has a STRICTLY smaller remaining time,
 * so equal remaining time never causes needless context switches.
 */
public class SRTFScheduler extends AbstractHeapScheduler {
    private static final Comparator<Process> ORDER = (a, b) -> {
        if (a.getRemainingTime() != b.getRemainingTime()) {
            return Integer.compare(a.getRemainingTime(), b.getRemainingTime());
        }
        if (a.getArrivalTime() != b.getArrivalTime()) {
            return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
        }
        return Process.comparePid(a.getPid(), b.getPid());
    };

    public SRTFScheduler(SimulationConfig config) {
        super("SRTF", config, ORDER);
    }

    @Override
    public boolean shouldPreempt(Process running) {
        Process best = ready.peek();
        return best != null && best.getRemainingTime() < running.getRemainingTime();
    }

    @Override
    public String getDataStructureDescription() {
        return "MyPriorityQueue - min-heap keyed by (remaining, arrival, PID)";
    }
}
