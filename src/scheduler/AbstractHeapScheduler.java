package scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import datastructures.MyPriorityQueue;
import model.Process;
import model.SimulationConfig;

/** Base for SJF, SRTF and Priority: one ready min-heap, different comparator / preemption rule. */
public abstract class AbstractHeapScheduler extends AbstractScheduler {
    protected final MyPriorityQueue<Process> ready;

    protected AbstractHeapScheduler(String name, SimulationConfig config, Comparator<Process> order) {
        super(name, config);
        this.ready = new MyPriorityQueue<>(order);
    }

    @Override
    public void admit(Process process) {
        ready.enqueue(process);
    }

    @Override
    public void requeue(Process process, RequeueReason reason) {
        ready.enqueue(process);
    }

    @Override
    public Process pickNext() {
        return ready.dequeue();
    }

    @Override
    public boolean hasReady() {
        return !ready.isEmpty();
    }

    @Override
    public List<String> queueNames() {
        List<String> names = new ArrayList<>();
        names.add("Ready Queue (Min-Heap, best first)");
        return names;
    }

    @Override
    public List<List<Process>> queueSnapshot() {
        List<List<Process>> snap = new ArrayList<>();
        snap.add(ready.toList());
        return snap;
    }
}
