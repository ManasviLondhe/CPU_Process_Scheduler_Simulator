package scheduler;

import model.Process;
import model.SimulationConfig;

/**
 * Multilevel Queue: a process stays in the queue of its class (Process.queueLevel, clamped to the
 * number of configured queues) for its whole life. Typical setup: 1 = System (Priority),
 * 2 = Interactive (Round Robin), 3 = Background (FCFS).
 * Lower queues can starve while higher queues are busy - the engine reports this as a starvation warning.
 */
public class MultilevelQueueScheduler extends MultiQueueScheduler {
    public MultilevelQueueScheduler(SimulationConfig config) {
        super("Multilevel Queue", config, config.getMlqLevels());
    }

    @Override
    protected int entryLevelIndex(Process process) {
        return Math.min(Math.max(process.getQueueLevel(), 1), levels.size()) - 1;
    }

    @Override
    public String getDataStructureDescription() {
        return "One data structure per level (MyQueue / MyCircularQueue / MyPriorityQueue) + fixed-priority preemptive inter-queue policy";
    }
}
