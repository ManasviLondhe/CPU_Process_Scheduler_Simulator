package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import datastructures.MinHeap;
import datastructures.MyLinkedList;
import metrics.MetricsCalculator;
import model.MetricsSummary;
import model.Process;
import model.ProcessState;
import model.ScheduleSegment;
import model.SegmentType;
import model.SimulationConfig;
import model.SimulationEvent;
import model.SimulationEventType;
import model.SimulationResult;
import model.SimulationState;
import scheduler.RequeueReason;
import scheduler.Scheduler;
import scheduler.SchedulerFactory;

/**
 * Discrete-time CPU simulation. Completely independent from Swing.
 * <p>
 * Every call to advanceOneTimeUnit() simulates the interval [t, t+1):
 * <ol>
 *   <li>admit processes whose arrival time is t (min-heap of future arrivals ordered by arrival, PID);</li>
 *   <li>if a context switch just finished, start the selected process;</li>
 *   <li>otherwise, if a process is running, check quantum expiry and preemption;</li>
 *   <li>if the CPU is free ask the scheduler for the next process (context switch if needed);</li>
 *   <li>execute one time unit (process / context switch / idle) and record the Gantt block;</li>
 *   <li>advance the clock, update waiting counters, let the scheduler age its queues.</li>
 * </ol>
 * Arrivals are admitted BEFORE a quantum-expired process is re-queued, so a process arriving exactly
 * when a quantum ends is placed ahead of it (classic Round Robin convention).
 * A context switch is counted whenever the CPU is given to a different process than the one that held it
 * immediately before (no switch after an idle period). It costs time only if enabled with cost &gt; 0.
 */
public class SimulationEngine {
    private static final long MAX_TICKS = 200_000_000L;
    private static final Comparator<Process> ARRIVAL_ORDER = (a, b) -> {
        if (a.getArrivalTime() != b.getArrivalTime()) {
            return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
        }
        return Process.comparePid(a.getPid(), b.getPid());
    };

    private final List<Process> workload = new ArrayList<>();
    private final SimulationConfig config;

    private List<Process> processes;
    private Scheduler scheduler;
    private MinHeap<Process> arrivals;
    private MyLinkedList<Process> completed;
    private CPU cpu;
    private SimulationClock clock;
    private EventManager events;
    private ContextSwitchManager switchManager;
    private List<ScheduleSegment> segments;
    private SimulationState status;
    private Process lastProcess;
    private boolean idleLogged;

    public SimulationEngine(List<Process> workload, SimulationConfig config) {
        for (Process p : workload) {
            this.workload.add(p.freshCopy());
        }
        this.config = config.copy();
        initialize();
    }

    private void initialize() {
        events = new EventManager();
        clock = new SimulationClock();
        cpu = new CPU();
        segments = new ArrayList<>();
        completed = new MyLinkedList<>();
        switchManager = new ContextSwitchManager(config.isContextSwitchEnabled(), config.getContextSwitchCost());
        processes = new ArrayList<>();
        arrivals = new MinHeap<>(ARRIVAL_ORDER);
        for (Process p : workload) {
            Process copy = p.freshCopy();
            processes.add(copy);
            arrivals.insert(copy);
        }
        scheduler = SchedulerFactory.create(config);
        scheduler.setQueueChangeListener((p, msg) ->
                events.add(clock.now(), SimulationEventType.PROCESS_QUEUE_CHANGE, p.getPid(), msg));
        lastProcess = null;
        idleLogged = false;
        status = SimulationState.NOT_STARTED;
    }

    // ------------------------------------------------------------------ control API

    public void startSimulation() {
        if (status == SimulationState.NOT_STARTED) {
            status = SimulationState.RUNNING;
        }
    }

    public void pauseSimulation() {
        if (status == SimulationState.RUNNING) {
            status = SimulationState.PAUSED;
        }
    }

    public void resumeSimulation() {
        if (status == SimulationState.PAUSED) {
            status = SimulationState.RUNNING;
        }
    }

    /** Executes exactly one time unit (step mode). */
    public void stepSimulation() {
        advanceOneTimeUnit();
    }

    public void stopSimulation() {
        if (!isFinished()) {
            status = SimulationState.STOPPED;
            events.add(clock.now(), SimulationEventType.SIMULATION_END, null, "Simulation stopped by user");
        }
    }

    public void resetSimulation() {
        initialize();
    }

    public boolean isFinished() {
        return status == SimulationState.COMPLETED || status == SimulationState.STOPPED;
    }

    /** Runs the whole simulation without UI and returns the result (used by comparison and tests). */
    public SimulationResult runToCompletion() {
        startSimulation();
        long guard = 0;
        while (!isFinished()) {
            if (guard++ > MAX_TICKS) {
                throw new IllegalStateException("Simulation did not terminate");
            }
            advanceOneTimeUnit();
        }
        return buildResult();
    }

    // ------------------------------------------------------------------ one time unit

    public void advanceOneTimeUnit() {
        if (isFinished()) {
            return;
        }
        if (status == SimulationState.NOT_STARTED) {
            status = SimulationState.PAUSED;
        }
        int now = clock.now();
        admitArrivals(now);

        Process yielded = null;
        if (cpu.isSwitching() && cpu.getSwitchRemaining() == 0) {
            startProcess(cpu.finishSwitch(), now);
        } else if (cpu.isRunning()) {
            yielded = checkQuantumAndPreemption(now);
        }
        if (cpu.isIdle()) {
            dispatch(now, yielded);
        }

        executeOneUnit(now);
        clock.tick();
        updateWaiting();
        scheduler.onTimeUnit();

        if (completed.size() == processes.size()) {
            status = SimulationState.COMPLETED;
            events.add(clock.now(), SimulationEventType.SIMULATION_END, null,
                    "All processes completed at t=" + clock.now());
        }
    }

    private void admitArrivals(int now) {
        while (arrivals.peek() != null && arrivals.peek().getArrivalTime() <= now) {
            Process p = arrivals.extractMin();
            p.setState(ProcessState.READY);
            scheduler.admit(p);
            events.add(now, SimulationEventType.PROCESS_ARRIVAL, p.getPid(),
                    p.getPid() + " arrived (burst " + p.getBurstTime() + ", priority " + p.getPriority()
                            + ") -> Queue " + p.getCurrentQueueLevel());
        }
    }

    /** Returns the process that was sent back to the ready structures, or null. */
    private Process checkQuantumAndPreemption(int now) {
        Process running = cpu.getProcess();
        int quantum = scheduler.quantumFor(running);
        RequeueReason reason = null;
        if (quantum > 0 && cpu.getSliceUsed() >= quantum) {
            reason = RequeueReason.QUANTUM_EXPIRED;
            events.add(now, SimulationEventType.QUANTUM_EXPIRED, running.getPid(),
                    running.getPid() + " used its quantum of " + quantum);
        } else if (scheduler.shouldPreempt(running)) {
            reason = RequeueReason.PREEMPTED;
        }
        if (reason == null) {
            return null;
        }
        running.setState(ProcessState.READY);
        cpu.release();
        scheduler.requeue(running, reason);
        return running;
    }

    private void dispatch(int now, Process yielded) {
        Process next = scheduler.pickNext();
        if (next == null) {
            return;
        }
        if (yielded != null && next != yielded) {
            yielded.incrementPreemptions();
            events.add(now, SimulationEventType.PREEMPTION, yielded.getPid(),
                    yielded.getPid() + " preempted (remaining " + yielded.getRemainingTime() + "), next: " + next.getPid());
        } else if (yielded != null) {
            events.add(now, SimulationEventType.PROCESS_QUEUE_CHANGE, yielded.getPid(),
                    yielded.getPid() + " continues (no other process is waiting)");
        }
        boolean switching = lastProcess != null && lastProcess != next;
        if (switching) {
            next.incrementContextSwitches();
            switchManager.recordSwitch();
            events.add(now, SimulationEventType.CONTEXT_SWITCH, next.getPid(),
                    "Context switch " + lastProcess.getPid() + " -> " + next.getPid()
                            + (switchManager.isCharged() ? " (cost " + switchManager.getCost() + ")" : " (no cost)"));
            if (switchManager.isCharged()) {
                cpu.beginSwitch(next, switchManager.getCost());
                return;
            }
        }
        startProcess(next, now);
    }

    private void startProcess(Process p, int now) {
        boolean first = p.getFirstStartTime() < 0;
        if (first) {
            p.setFirstStartTime(now);
        }
        p.setState(ProcessState.RUNNING);
        p.setWaitStreak(0);
        p.setStarvationFlagged(false);
        cpu.run(p);
        events.add(now, SimulationEventType.CPU_START, p.getPid(),
                p.getPid() + (first ? " started execution" : " resumed execution")
                        + " (remaining " + p.getRemainingTime() + ")");
    }

    private void executeOneUnit(int now) {
        if (cpu.isSwitching()) {
            addSegment(SegmentType.CONTEXT_SWITCH, null, now);
            cpu.tickSwitch();
            switchManager.addTime(1);
            idleLogged = false;
        } else if (cpu.isRunning()) {
            Process p = cpu.getProcess();
            addSegment(SegmentType.PROCESS, p.getPid(), now);
            p.consumeOneUnit();
            cpu.addSlice();
            lastProcess = p;
            idleLogged = false;
            if (p.getRemainingTime() == 0) {
                complete(p, now + 1);
            }
        } else {
            if (!idleLogged) {
                events.add(now, SimulationEventType.CPU_IDLE, null, "CPU idle - no ready process");
                idleLogged = true;
            }
            addSegment(SegmentType.IDLE, null, now);
            lastProcess = null;
        }
    }

    private void complete(Process p, int endTime) {
        p.setCompletionTime(endTime);
        p.setState(ProcessState.COMPLETED);
        MetricsCalculator.applyProcessMetrics(p);
        completed.add(p);
        cpu.release();
        events.add(endTime, SimulationEventType.CPU_COMPLETE, p.getPid(),
                p.getPid() + " completed (TAT " + p.getTurnaroundTime() + ", WT " + p.getWaitingTime() + ")");
    }

    private void addSegment(SegmentType type, String pid, int now) {
        ScheduleSegment last = segments.isEmpty() ? null : segments.get(segments.size() - 1);
        boolean samePid = last != null && (pid == null ? last.getPid() == null : pid.equals(last.getPid()));
        if (last != null && last.getType() == type && samePid && last.getEnd() == now) {
            last.setEnd(now + 1);
        } else {
            segments.add(new ScheduleSegment(type, pid, now, now + 1));
        }
    }

    private void updateWaiting() {
        int threshold = config.getStarvationThreshold();
        for (Process p : processes) {
            if (p.getState() != ProcessState.READY) {
                continue;
            }
            p.incrementWaiting();
            if (threshold > 0 && !p.isStarvationFlagged() && p.getWaitStreak() >= threshold) {
                p.setStarvationFlagged(true);
                events.add(clock.now(), SimulationEventType.STARVATION_WARNING, p.getPid(),
                        "Potential starvation detected: " + p.getPid() + " has waited " + p.getWaitStreak()
                                + " units without CPU");
            }
        }
    }

    // ------------------------------------------------------------------ read access for UI / services

    public SimulationResult buildResult() {
        List<Process> snapshot = new ArrayList<>();
        for (Process p : processes) {
            snapshot.add(p.snapshot());
        }
        List<ScheduleSegment> segs = new ArrayList<>();
        for (ScheduleSegment s : segments) {
            segs.add(new ScheduleSegment(s.getType(), s.getPid(), s.getStart(), s.getEnd()));
        }
        return new SimulationResult(config.getAlgorithm().getDisplayName(), config, snapshot, segs,
                new ArrayList<>(events.getEvents()), getLiveMetrics());
    }

    public MetricsSummary getLiveMetrics() {
        return MetricsCalculator.calculate(processes, segments, clock.now(),
                switchManager.getSwitchCount(), switchManager.getTotalTime());
    }

    public int getCurrentTime() { return clock.now(); }
    public SimulationState getStatus() { return status; }
    public CPU getCpu() { return cpu; }
    public Scheduler getScheduler() { return scheduler; }
    public SimulationConfig getConfig() { return config; }
    public List<Process> getProcesses() { return Collections.unmodifiableList(processes); }
    public List<ScheduleSegment> getSegments() { return Collections.unmodifiableList(segments); }
    public List<SimulationEvent> getEvents() { return events.getEvents(); }
    public int getCompletedCount() { return completed.size(); }
    public int getQuantumOfRunning() {
        return cpu.getProcess() == null ? -1 : scheduler.quantumFor(cpu.getProcess());
    }
}
