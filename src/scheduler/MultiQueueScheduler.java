package scheduler;

import java.util.ArrayList;
import java.util.List;

import datastructures.MyCircularQueue;
import datastructures.MyPriorityQueue;
import datastructures.MyQueue;
import datastructures.QueueADT;
import model.Process;
import model.QueueLevelConfig;
import model.QueuePolicy;
import model.SimulationConfig;

/**
 * Shared base of MLQ and MLFQ: an array of levels, each level backed by the data structure that
 * matches its policy (FCFS -> MyQueue, Round Robin -> MyCircularQueue, Priority -> MyPriorityQueue).
 * <p>
 * Inter-queue policy: FIXED PRIORITY, PREEMPTIVE. Level 1 is served before level 2 and so on;
 * a process arriving in a higher level preempts a running process of a lower level.
 * Inside a level its own policy applies (priority levels are non-preemptive inside the level).
 */
public abstract class MultiQueueScheduler extends AbstractScheduler {
    protected final List<QueueLevelConfig> levels;
    protected final List<QueueADT<Process>> queues = new ArrayList<>();

    protected MultiQueueScheduler(String name, SimulationConfig config, List<QueueLevelConfig> levels) {
        super(name, config);
        this.levels = levels;
        for (QueueLevelConfig level : levels) {
            queues.add(createQueue(level, config.isPriorityLowerIsHigher()));
        }
    }

    private static QueueADT<Process> createQueue(QueueLevelConfig level, boolean lowerIsHigher) {
        switch (level.getPolicy()) {
            case ROUND_ROBIN:
                return new MyCircularQueue<>(8);
            case PRIORITY:
                return new MyPriorityQueue<>((a, b) -> {
                    if (a.getPriority() != b.getPriority()) {
                        return lowerIsHigher ? Integer.compare(a.getPriority(), b.getPriority())
                                             : Integer.compare(b.getPriority(), a.getPriority());
                    }
                    if (a.getArrivalTime() != b.getArrivalTime()) {
                        return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
                    }
                    return Process.comparePid(a.getPid(), b.getPid());
                });
            default:
                return new MyQueue<>();
        }
    }

    /** 0-based level where a newly arrived process starts. */
    protected abstract int entryLevelIndex(Process process);

    /** 0-based level where a process goes when it returns to the queues. */
    protected int levelOnRequeue(Process process, RequeueReason reason) {
        return levelIndex(process);
    }

    protected int levelIndex(Process process) {
        return process.getCurrentQueueLevel() - 1;
    }

    @Override
    public void admit(Process process) {
        place(process, entryLevelIndex(process));
    }

    @Override
    public void requeue(Process process, RequeueReason reason) {
        place(process, levelOnRequeue(process, reason));
    }

    private void place(Process process, int index) {
        process.setCurrentQueueLevel(index + 1);
        process.setAgingCounter(0);
        queues.get(index).enqueue(process);
    }

    @Override
    public Process pickNext() {
        for (QueueADT<Process> q : queues) {
            if (!q.isEmpty()) {
                Process p = q.dequeue();
                p.setAgingCounter(0);
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean hasReady() {
        for (QueueADT<Process> q : queues) {
            if (!q.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPreempt(Process running) {
        for (int i = 0; i < levelIndex(running); i++) {
            if (!queues.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int quantumFor(Process process) {
        QueueLevelConfig level = levels.get(levelIndex(process));
        return level.getPolicy() == QueuePolicy.ROUND_ROBIN ? level.getQuantum() : NO_QUANTUM;
    }

    @Override
    public List<String> queueNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < levels.size(); i++) {
            QueueLevelConfig l = levels.get(i);
            String extra = l.getPolicy() == QueuePolicy.ROUND_ROBIN ? ", q=" + l.getQuantum() : "";
            names.add("Queue " + (i + 1) + " - " + l.getName() + " [" + l.getPolicy() + extra + "]");
        }
        return names;
    }

    @Override
    public List<List<Process>> queueSnapshot() {
        List<List<Process>> snap = new ArrayList<>();
        for (QueueADT<Process> q : queues) {
            snap.add(q.toList());
        }
        return snap;
    }
}
