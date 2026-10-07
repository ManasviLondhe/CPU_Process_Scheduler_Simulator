package scheduler;

import java.util.Comparator;
import java.util.List;

import model.Process;
import model.SimulationConfig;

/**
 * Preemptive priority scheduling with optional aging.
 * <p>
 * Direction: lowerIsHigher = true  -> smaller number is more important;
 *            lowerIsHigher = false -> larger number is more important.
 * Key: (effective priority, arrival time, PID). Preempts only on a STRICTLY better priority.
 * <p>
 * AGING RULE: every ready process has an aging counter that grows by 1 per time unit it waits.
 * When the counter reaches the aging interval, its effective priority improves by one step
 * (number - 1 or + 1, never below 0 when lower-is-higher) and the counter restarts.
 * The boost is KEPT while the process runs (so it cannot be preempted by the very job it overtook) and
 * the effective priority returns to the base priority when the process is re-queued after a preemption.
 * Because keys change, the heap is rebuilt (O(n)) after an aging step.
 */
public class PriorityScheduler extends AbstractHeapScheduler {
    private final boolean lowerIsHigher;

    public PriorityScheduler(SimulationConfig config) {
        super("Priority", config, order(config.isPriorityLowerIsHigher()));
        this.lowerIsHigher = config.isPriorityLowerIsHigher();
    }

    private static Comparator<Process> order(boolean lowerIsHigher) {
        return (a, b) -> {
            if (a.getEffectivePriority() != b.getEffectivePriority()) {
                return lowerIsHigher
                        ? Integer.compare(a.getEffectivePriority(), b.getEffectivePriority())
                        : Integer.compare(b.getEffectivePriority(), a.getEffectivePriority());
            }
            if (a.getArrivalTime() != b.getArrivalTime()) {
                return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
            }
            return Process.comparePid(a.getPid(), b.getPid());
        };
    }

    private boolean strictlyBetter(Process a, Process b) {
        return lowerIsHigher ? a.getEffectivePriority() < b.getEffectivePriority()
                             : a.getEffectivePriority() > b.getEffectivePriority();
    }

    @Override
    public void requeue(Process process, RequeueReason reason) {
        process.setAgingCounter(0);
        process.setEffectivePriority(process.getPriority());
        super.requeue(process, reason);
    }

    @Override
    public Process pickNext() {
        Process p = super.pickNext();
        if (p != null) {
            p.setAgingCounter(0);
        }
        return p;
    }

    @Override
    public boolean shouldPreempt(Process running) {
        Process best = ready.peek();
        return best != null && strictlyBetter(best, running);
    }

    @Override
    public void onTimeUnit() {
        if (!config.isAgingEnabled() || config.getAgingInterval() <= 0 || ready.isEmpty()) {
            return;
        }
        boolean changed = false;
        List<Process> waiting = ready.toList();
        for (Process p : waiting) {
            p.setAgingCounter(p.getAgingCounter() + 1);
            if (p.getAgingCounter() >= config.getAgingInterval()) {
                p.setAgingCounter(0);
                int old = p.getEffectivePriority();
                int better = lowerIsHigher ? Math.max(0, old - 1) : old + 1;
                if (better != old) {
                    p.setEffectivePriority(better);
                    changed = true;
                    notifyQueueChange(p, p.getPid() + " aged: priority " + old + " -> " + better);
                }
            }
        }
        if (changed) {
            ready.rebuild();
        }
    }

    @Override
    public String getDataStructureDescription() {
        return "MyPriorityQueue - min-heap keyed by (effective priority, arrival, PID); aging triggers heap rebuild";
    }
}
