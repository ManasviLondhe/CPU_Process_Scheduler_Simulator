package scheduler;

import java.util.List;
import java.util.function.BiConsumer;

import model.Process;

/**
 * Strategy interface implemented by every scheduling algorithm.
 * A scheduler only DECIDES (it stores ready processes in a data structure and answers
 * "who runs next?" / "should the running process be preempted?").
 * The SimulationEngine owns time, CPU and events.
 */
public interface Scheduler {
    /** Value returned by quantumFor when there is no time slice. */
    int NO_QUANTUM = -1;

    String getName();

    /** Text shown in the UI describing the data structure(s) used. */
    String getDataStructureDescription();

    /** A process has just arrived and becomes READY. */
    void admit(Process process);

    /** The running process goes back to the ready structures (preempted or quantum expired). */
    void requeue(Process process, RequeueReason reason);

    /** Removes and returns the next process to run, or null if none is ready. */
    Process pickNext();

    boolean hasReady();

    /** Time slice for the given process, or NO_QUANTUM. */
    int quantumFor(Process process);

    /** True if a waiting process is strictly better than the running one. */
    boolean shouldPreempt(Process running);

    /** Called once after every elapsed time unit (aging, bookkeeping). */
    void onTimeUnit();

    List<String> queueNames();

    /** Snapshot of every ready structure in service order (for the UI). */
    List<List<Process>> queueSnapshot();

    /** Lets the scheduler report queue changes (demotion, promotion, aging) to the engine. */
    void setQueueChangeListener(BiConsumer<Process, String> listener);
}
