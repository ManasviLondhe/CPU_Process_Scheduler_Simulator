package scheduler;

import java.util.ArrayList;
import java.util.List;

import datastructures.MyCircularQueue;
import model.Process;
import model.SimulationConfig;

/**
 * Round Robin with a configurable quantum.
 * Data structure: MyCircularQueue. A process that used its quantum is enqueued at the rear
 * AFTER processes that arrived at that same instant (the engine admits arrivals first),
 * which is the classic textbook convention. Enqueue/dequeue are O(1).
 */
public class RoundRobinScheduler extends AbstractScheduler {
    private final MyCircularQueue<Process> ready = new MyCircularQueue<>(8);

    public RoundRobinScheduler(SimulationConfig config) {
        super("Round Robin", config);
    }

    @Override
    public int quantumFor(Process process) {
        return config.getTimeQuantum();
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
        names.add("Circular Ready Queue (quantum = " + config.getTimeQuantum() + ")");
        return names;
    }

    @Override
    public List<List<Process>> queueSnapshot() {
        List<List<Process>> snap = new ArrayList<>();
        snap.add(ready.toList());
        return snap;
    }

    @Override
    public String getDataStructureDescription() {
        return "MyCircularQueue - array with wrap-around front/rear indices";
    }
}
