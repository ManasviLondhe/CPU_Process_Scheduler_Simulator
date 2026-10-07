package scheduler;

import java.util.ArrayList;
import java.util.List;

import datastructures.QueueADT;
import model.Process;
import model.SimulationConfig;

/**
 * Multilevel Feedback Queue.
 * RULES (exactly as implemented):
 * 1. Every process starts in level 1.
 * 2. Level 1 is served before level 2, etc.; a higher level preempts a running lower-level process
 *    (the preempted process goes to the tail of its own level).
 * 3. DEMOTION: when a process uses its whole quantum in a Round Robin level that is not the last one,
 *    it moves to the tail of the next level. In the last level it stays (tail of the same level).
 * 4. FCFS levels have no quantum: the process runs until it finishes or is preempted.
 * 5. AGING / PROMOTION (starvation prevention, only if aging is enabled): every process waiting in
 *    level k > 1 gets +1 aging counter per time unit; at the threshold it is promoted to the tail of
 *    level k-1 and the counter restarts. The counter also restarts whenever the process is dispatched
 *    or re-queued.
 */
public class MLFQScheduler extends MultiQueueScheduler {
    public MLFQScheduler(SimulationConfig config) {
        super("MLFQ", config, config.getMlfqLevels());
    }

    @Override
    protected int entryLevelIndex(Process process) {
        return 0;
    }

    @Override
    protected int levelOnRequeue(Process process, RequeueReason reason) {
        int index = levelIndex(process);
        if (reason == RequeueReason.QUANTUM_EXPIRED && index < levels.size() - 1) {
            notifyQueueChange(process, process.getPid() + " demoted: Queue " + (index + 1) + " -> Queue " + (index + 2));
            return index + 1;
        }
        return index;
    }

    @Override
    public void onTimeUnit() {
        if (!config.isAgingEnabled() || config.getAgingInterval() <= 0) {
            return;
        }
        for (int i = 1; i < queues.size(); i++) {
            QueueADT<Process> queue = queues.get(i);
            if (queue.isEmpty()) {
                continue;
            }
            List<Process> waiting = queue.toList();
            List<Process> promoted = new ArrayList<>();
            for (Process p : waiting) {
                p.setAgingCounter(p.getAgingCounter() + 1);
                if (p.getAgingCounter() >= config.getAgingInterval()) {
                    promoted.add(p);
                }
            }
            if (promoted.isEmpty()) {
                continue;
            }
            queue.clear();
            for (Process p : waiting) {
                if (!promoted.contains(p)) {
                    queue.enqueue(p);
                }
            }
            for (Process p : promoted) {
                p.setAgingCounter(0);
                p.setCurrentQueueLevel(i);
                queues.get(i - 1).enqueue(p);
                notifyQueueChange(p, p.getPid() + " promoted by aging: Queue " + (i + 1) + " -> Queue " + i);
            }
        }
    }

    @Override
    public String getDataStructureDescription() {
        return "Array of queues (MyQueue / MyCircularQueue per level) with dynamic movement of processes between levels";
    }
}
