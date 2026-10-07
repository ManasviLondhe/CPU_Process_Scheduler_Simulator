package scheduler;

import java.util.ArrayList;
import java.util.List;

import datastructures.MyQueue;
import model.Process;
import model.SimulationConfig;

/**
 * First Come First Serve (non-preemptive).
 * Data structure: MyQueue (FIFO). The engine admits arrivals ordered by (arrival, PID),
 * so insertion order == arrival order and no sorting is needed here.
 * Cost: enqueue/dequeue O(1) -> whole schedule O(n) after the O(n log n) arrival ordering.
 */
public class FCFSScheduler extends AbstractScheduler {
    private final MyQueue<Process> ready = new MyQueue<>();

    public FCFSScheduler(SimulationConfig config) {
        super("FCFS", config);
    }

    @Override
    public String getDataStructureDescription() {
        return "MyQueue - linked FIFO queue (head = next to run)";
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
        names.add("Ready Queue (FIFO)");
        return names;
    }

    @Override
    public List<List<Process>> queueSnapshot() {
        List<List<Process>> snap = new ArrayList<>();
        snap.add(ready.toList());
        return snap;
    }
}
