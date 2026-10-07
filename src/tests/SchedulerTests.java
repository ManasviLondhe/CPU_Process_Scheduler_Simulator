package tests;

import static tests.TestUtil.assertClose;
import static tests.TestUtil.assertEquals;
import static tests.TestUtil.check;
import static tests.TestUtil.gantt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comparison.AlgorithmComparisonService;
import generator.SampleWorkloads;
import model.Process;
import model.ProcessState;
import model.QueueLevelConfig;
import model.QueuePolicy;
import model.SchedulingAlgorithmType;
import model.SimulationConfig;
import model.SimulationEventType;
import model.SimulationResult;
import model.SimulationState;
import scheduler.PriorityScheduler;
import simulation.SimulationEngine;

/**
 * Scheduler / engine tests. Every expected Gantt chart and metric below was calculated BY HAND
 * from the algorithm definition (see PROJECT_DOCUMENTATION.md, "Hand-verified test cases").
 */
public final class SchedulerTests {
    private SchedulerTests() {
    }

    private static SimulationConfig cfg(SchedulingAlgorithmType type) {
        SimulationConfig c = new SimulationConfig();
        c.setAlgorithm(type);
        return c;
    }

    private static SimulationResult run(List<Process> workload, SimulationConfig c) {
        return new SimulationEngine(workload, c).runToCompletion();
    }

    private static Process find(SimulationResult r, String pid) {
        for (Process p : r.getProcesses()) {
            if (p.getPid().equals(pid)) {
                return p;
            }
        }
        throw new IllegalStateException("missing " + pid);
    }

    private static List<Process> list(Process... ps) {
        return new ArrayList<>(Arrays.asList(ps));
    }

    public static void run() {
        fcfs();
        sjf();
        srtf();
        priority();
        roundRobin();
        contextSwitching();
        multilevel();
        edgeCases();
        engineControl();
        comparison();
    }

    private static void fcfs() {
        TestUtil.section("FCFS");
        SimulationResult r = run(SampleWorkloads.basic(), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("gantt", "P1:0-5 P2:5-8 P3:8-10", gantt(r.getSegments()));
        assertEquals("P1 CT", 5, find(r, "P1").getCompletionTime());
        assertEquals("P2 WT", 4, find(r, "P2").getWaitingTime());
        assertEquals("P3 TAT", 8, find(r, "P3").getTurnaroundTime());
        assertEquals("P3 RT", 6, find(r, "P3").getResponseTime());
        assertClose("avg WT", 10.0 / 3, r.getMetrics().getAvgWaitingTime());
        assertClose("avg TAT", 20.0 / 3, r.getMetrics().getAvgTurnaroundTime());
        assertClose("utilization", 100.0, r.getMetrics().getCpuUtilization());

        r = run(SampleWorkloads.idle(), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("idle gantt", "P1:0-3 IDLE:3-8 P2:8-12", gantt(r.getSegments()));
        assertEquals("idle time", 5, r.getMetrics().getIdleTime());
        assertClose("idle utilization", 700.0 / 12, r.getMetrics().getCpuUtilization());
        assertEquals("no context switch across idle gap", 0, r.getMetrics().getContextSwitches());

        r = run(list(new Process("P1", 0, 2), new Process("P2", 2, 2)), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("arrival exactly at completion", "P1:0-2 P2:2-4", gantt(r.getSegments()));
    }

    private static void sjf() {
        TestUtil.section("SJF");
        SimulationResult r = run(SampleWorkloads.basic(), cfg(SchedulingAlgorithmType.SJF));
        assertEquals("gantt", "P1:0-5 P3:5-7 P2:7-10", gantt(r.getSegments()));
        assertClose("avg WT", 3.0, r.getMetrics().getAvgWaitingTime());
        assertEquals("P2 RT", 6, find(r, "P2").getResponseTime());
        assertEquals("P3 RT", 3, find(r, "P3").getResponseTime());
    }

    private static void srtf() {
        TestUtil.section("SRTF");
        SimulationResult r = run(SampleWorkloads.basic(), cfg(SchedulingAlgorithmType.SRTF));
        assertEquals("gantt", "P1:0-1 P2:1-4 P3:4-6 P1:6-10", gantt(r.getSegments()));
        assertClose("avg WT", 7.0 / 3, r.getMetrics().getAvgWaitingTime());
        assertEquals("P1 preemptions", 1, find(r, "P1").getPreemptions());
        assertEquals("P3 RT", 2, find(r, "P3").getResponseTime());

        r = run(SampleWorkloads.srtfPreemption(), cfg(SchedulingAlgorithmType.SRTF));
        assertEquals("4-process gantt", "P1:0-1 P2:1-2 P3:2-4 P4:4-5 P2:5-8 P1:8-15", gantt(r.getSegments()));
        assertClose("4-process avg WT", 2.75, r.getMetrics().getAvgWaitingTime());
        assertEquals("total preemptions", 2, r.getMetrics().getPreemptions());
        assertEquals("P4 RT", 1, find(r, "P4").getResponseTime());
    }

    private static void priority() {
        TestUtil.section("Priority");
        SimulationConfig c = cfg(SchedulingAlgorithmType.PRIORITY);
        SimulationResult r = run(SampleWorkloads.priorityPreemption(), c);
        assertEquals("lower-is-higher gantt", "P1:0-2 P2:2-6 P3:6-8 P1:8-12", gantt(r.getSegments()));
        assertClose("avg WT", 3.0, r.getMetrics().getAvgWaitingTime());
        assertEquals("P1 preemptions", 1, find(r, "P1").getPreemptions());

        c.setPriorityLowerIsHigher(false);
        r = run(SampleWorkloads.priorityPreemption(), c);
        assertEquals("higher-is-higher gantt", "P1:0-6 P3:6-8 P2:8-12", gantt(r.getSegments()));

        r = run(list(new Process("P1", 0, 2, 5), new Process("P2", 0, 2, 5), new Process("P3", 0, 2, 5)),
                cfg(SchedulingAlgorithmType.PRIORITY));
        assertEquals("same priority -> arrival then PID", "P1:0-2 P2:2-4 P3:4-6", gantt(r.getSegments()));

        SimulationConfig aging = cfg(SchedulingAlgorithmType.PRIORITY);
        aging.setAgingEnabled(true);
        aging.setAgingInterval(2);
        PriorityScheduler s = new PriorityScheduler(aging);
        Process low = new Process("L", 0, 5, 5);
        s.admit(low);
        s.onTimeUnit();
        assertEquals("no aging before interval", 5, low.getEffectivePriority());
        s.onTimeUnit();
        assertEquals("aged once after 2 units", 4, low.getEffectivePriority());
        s.onTimeUnit();
        s.onTimeUnit();
        assertEquals("aged twice after 4 units", 3, low.getEffectivePriority());
        assertEquals("dispatch returns same process", low, s.pickNext());
        assertEquals("boost kept while running", 3, low.getEffectivePriority());
        s.requeue(low, scheduler.RequeueReason.PREEMPTED);
        assertEquals("effective priority reset when re-queued", 5, low.getEffectivePriority());

        // Aging changes the real schedule: P2 (priority 9) waits behind a stream of priority 1 jobs.
        List<Process> stream = list(new Process("P2", 0, 2, 2), new Process("H1", 0, 2, 1),
                new Process("H2", 2, 2, 1), new Process("H3", 4, 2, 1));
        assertEquals("starving without aging", "H1:0-2 H2:2-4 H3:4-6 P2:6-8",
                gantt(run(stream, cfg(SchedulingAlgorithmType.PRIORITY)).getSegments()));
        SimulationConfig fast = cfg(SchedulingAlgorithmType.PRIORITY);
        fast.setAgingEnabled(true);
        fast.setAgingInterval(2);
        SimulationResult aged = run(stream, fast);
        assertEquals("aging lets P2 overtake the stream", "H1:0-2 P2:2-4 H2:4-6 H3:6-8", gantt(aged.getSegments()));
        assertEquals("aged P2 is not preempted", 0, find(aged, "P2").getPreemptions());
    }

    private static void roundRobin() {
        TestUtil.section("Round Robin");
        SimulationConfig c = cfg(SchedulingAlgorithmType.ROUND_ROBIN);
        c.setTimeQuantum(2);
        SimulationResult r = run(SampleWorkloads.roundRobin(), c);
        assertEquals("gantt", "P1:0-2 P2:2-4 P3:4-6 P1:6-8 P2:8-10 P3:10-11 P1:11-12", gantt(r.getSegments()));
        assertEquals("P1 CT", 12, find(r, "P1").getCompletionTime());
        assertEquals("P2 CT", 10, find(r, "P2").getCompletionTime());
        assertEquals("P3 CT", 11, find(r, "P3").getCompletionTime());
        assertClose("avg WT", 7.0, r.getMetrics().getAvgWaitingTime());
        assertClose("avg TAT", 11.0, r.getMetrics().getAvgTurnaroundTime());
        assertClose("avg RT", 2.0, r.getMetrics().getAvgResponseTime());
        assertEquals("context switches", 6, r.getMetrics().getContextSwitches());
        assertEquals("preemptions", 4, r.getMetrics().getPreemptions());

        r = run(list(new Process("P1", 0, 4), new Process("P2", 2, 2)), c);
        assertEquals("arrival exactly at quantum expiry goes first", "P1:0-2 P2:2-4 P1:4-6", gantt(r.getSegments()));

        r = run(list(new Process("P1", 0, 5)), c);
        assertEquals("single process keeps CPU", "P1:0-5", gantt(r.getSegments()));
        assertEquals("single process: no preemption", 0, r.getMetrics().getPreemptions());
        assertEquals("single process: no switch", 0, r.getMetrics().getContextSwitches());

        c.setTimeQuantum(8);
        r = run(SampleWorkloads.roundRobin(), c);
        assertEquals("huge quantum behaves like FCFS", "P1:0-5 P2:5-9 P3:9-12", gantt(r.getSegments()));
    }

    private static void contextSwitching() {
        TestUtil.section("Context switching");
        SimulationConfig c = cfg(SchedulingAlgorithmType.FCFS);
        c.setContextSwitchEnabled(true);
        c.setContextSwitchCost(1);
        SimulationResult r = run(list(new Process("P1", 0, 2), new Process("P2", 0, 2)), c);
        assertEquals("gantt with CS", "P1:0-2 CS:2-3 P2:3-5", gantt(r.getSegments()));
        assertEquals("switch count", 1, r.getMetrics().getContextSwitches());
        assertEquals("switch time", 1, r.getMetrics().getContextSwitchTime());
        assertClose("utilization excludes switch overhead", 80.0, r.getMetrics().getCpuUtilization());
        assertEquals("P2 WT includes switch", 3, find(r, "P2").getWaitingTime());

        SimulationConfig rr = cfg(SchedulingAlgorithmType.ROUND_ROBIN);
        rr.setTimeQuantum(2);
        rr.setContextSwitchEnabled(true);
        rr.setContextSwitchCost(1);
        r = run(SampleWorkloads.roundRobin(), rr);
        assertEquals("RR with CS gantt",
                "P1:0-2 CS:2-3 P2:3-5 CS:5-6 P3:6-8 CS:8-9 P1:9-11 CS:11-12 P2:12-14 CS:14-15 P3:15-16 CS:16-17 P1:17-18",
                gantt(r.getSegments()));
        assertEquals("RR with CS total time", 18, r.getMetrics().getTotalTime());
        assertEquals("RR with CS switch time", 6, r.getMetrics().getContextSwitchTime());

        c.setContextSwitchEnabled(false);
        r = run(list(new Process("P1", 0, 2), new Process("P2", 0, 2)), c);
        assertEquals("disabled: no extra block", "P1:0-2 P2:2-4", gantt(r.getSegments()));
        assertEquals("disabled: switch still counted", 1, r.getMetrics().getContextSwitches());
        assertEquals("disabled: no switch time", 0, r.getMetrics().getContextSwitchTime());
    }

    private static void multilevel() {
        TestUtil.section("MLQ / MLFQ");
        SimulationResult r = run(SampleWorkloads.mlqMixed(), cfg(SchedulingAlgorithmType.MLQ));
        assertEquals("MLQ gantt", "A:0-1 B:1-2 C:2-4 B:4-6 A:6-8", gantt(r.getSegments()));
        assertClose("MLQ avg WT", 7.0 / 3, r.getMetrics().getAvgWaitingTime());
        assertEquals("MLQ preemptions", 2, r.getMetrics().getPreemptions());

        r = run(list(new Process("P1", 0, 7), new Process("P2", 1, 2)), cfg(SchedulingAlgorithmType.MLFQ));
        assertEquals("MLFQ demotion gantt", "P1:0-2 P2:2-4 P1:4-9", gantt(r.getSegments()));
        assertEquals("P1 ends in level 3", 3, find(r, "P1").getCurrentQueueLevel());
        assertEquals("P1 CT", 9, find(r, "P1").getCompletionTime());
        assertEquals("P2 RT", 1, find(r, "P2").getResponseTime());
        assertEquals("P1 preemptions (continuing alone is not a preemption)", 1, find(r, "P1").getPreemptions());
        boolean demotionLogged = false;
        for (model.SimulationEvent e : r.getEvents()) {
            demotionLogged |= e.getType() == SimulationEventType.PROCESS_QUEUE_CHANGE && e.getMessage().contains("demoted");
        }
        check("demotion events logged", demotionLogged);

        SimulationConfig c = cfg(SchedulingAlgorithmType.MLFQ);
        List<QueueLevelConfig> levels = new ArrayList<>();
        levels.add(new QueueLevelConfig("L1", QueuePolicy.ROUND_ROBIN, 2));
        levels.add(new QueueLevelConfig("L2", QueuePolicy.FCFS, 2));
        c.setMlfqLevels(levels);
        r = run(SampleWorkloads.mlfqStarvation(), c);
        assertEquals("MLFQ without aging", "P1:0-2 S1:2-4 S2:4-6 S3:6-8 S4:8-10 P1:10-14", gantt(r.getSegments()));
        c.setAgingEnabled(true);
        c.setAgingInterval(4);
        r = run(SampleWorkloads.mlfqStarvation(), c);
        assertEquals("MLFQ with aging (promotion)", "P1:0-2 S1:2-4 S2:4-6 P1:6-8 S3:8-10 S4:10-12 P1:12-14",
                gantt(r.getSegments()));
        assertEquals("S3 completion", 10, find(r, "S3").getCompletionTime());
        assertEquals("P1 waiting", 8, find(r, "P1").getWaitingTime());
        boolean promoted = false;
        for (model.SimulationEvent e : r.getEvents()) {
            promoted |= e.getMessage().contains("promoted");
        }
        check("promotion event logged", promoted);

        SimulationConfig single = cfg(SchedulingAlgorithmType.MLFQ);
        List<QueueLevelConfig> one = new ArrayList<>();
        one.add(new QueueLevelConfig("Only", QueuePolicy.ROUND_ROBIN, 2));
        single.setMlfqLevels(one);
        r = run(SampleWorkloads.roundRobin(), single);
        assertEquals("single-level MLFQ equals Round Robin", "P1:0-2 P2:2-4 P3:4-6 P1:6-8 P2:8-10 P3:10-11 P1:11-12",
                gantt(r.getSegments()));
    }

    private static void edgeCases() {
        TestUtil.section("Edge cases");
        SimulationResult r = run(list(new Process("P2", 0, 1), new Process("P10", 0, 1)), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("natural PID order (P2 before P10)", "P2:0-1 P10:1-2", gantt(r.getSegments()));

        r = run(list(new Process("A", 0, 3), new Process("B", 0, 3), new Process("C", 0, 3)), cfg(SchedulingAlgorithmType.SJF));
        assertEquals("equal bursts -> PID order", "A:0-3 B:3-6 C:6-9", gantt(r.getSegments()));

        r = run(list(new Process("P1", 0, 100_000)), cfg(SchedulingAlgorithmType.SRTF));
        assertEquals("very large burst", 100_000, find(r, "P1").getCompletionTime());
        assertEquals("one merged segment", 1, r.getSegments().size());

        r = run(list(new Process("P1", 5, 2)), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("late first arrival starts with idle", "IDLE:0-5 P1:5-7", gantt(r.getSegments()));

        SimulationConfig starve = cfg(SchedulingAlgorithmType.FCFS);
        starve.setStarvationThreshold(5);
        r = run(list(new Process("P1", 0, 10), new Process("P2", 0, 1)), starve);
        boolean warned = false;
        for (model.SimulationEvent e : r.getEvents()) {
            warned |= e.getType() == SimulationEventType.STARVATION_WARNING && e.getMessage().contains("P2");
        }
        check("starvation warning for P2", warned);

        List<Process> many = new ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            many.add(new Process("P" + i, i % 7, 1 + i % 5, i % 4));
        }
        for (SchedulingAlgorithmType t : SchedulingAlgorithmType.values()) {
            SimulationResult big = run(many, cfg(t));
            assertEquals(t + ": all 150 completed", 150, big.getMetrics().getCompletedCount());
            int units = 0;
            for (model.ScheduleSegment s : big.getSegments()) {
                units += s.getLength();
            }
            assertEquals(t + ": gantt covers total time", big.getMetrics().getTotalTime(), units);
        }
    }

    private static void engineControl() {
        TestUtil.section("Engine control / states");
        SimulationEngine e = new SimulationEngine(SampleWorkloads.basic(), cfg(SchedulingAlgorithmType.FCFS));
        assertEquals("initial status", SimulationState.NOT_STARTED, e.getStatus());
        assertEquals("process NEW before start", ProcessState.NEW, e.getProcesses().get(0).getState());
        e.stepSimulation();
        assertEquals("one step = one time unit", 1, e.getCurrentTime());
        assertEquals("P1 running", ProcessState.RUNNING, e.getProcesses().get(0).getState());
        assertEquals("P2 still NEW at t=1", ProcessState.NEW, e.getProcesses().get(1).getState());
        e.stepSimulation();
        assertEquals("P2 READY at t=2", ProcessState.READY, e.getProcesses().get(1).getState());
        e.startSimulation();
        e.resumeSimulation();
        e.pauseSimulation();
        assertEquals("paused", SimulationState.PAUSED, e.getStatus());
        e.resumeSimulation();
        assertEquals("resumed", SimulationState.RUNNING, e.getStatus());
        e.resetSimulation();
        assertEquals("reset time", 0, e.getCurrentTime());
        assertEquals("reset status", SimulationState.NOT_STARTED, e.getStatus());
        assertEquals("reset clears gantt", 0, e.getSegments().size());
        e.startSimulation();
        while (!e.isFinished()) {
            e.advanceOneTimeUnit();
        }
        assertEquals("completed", SimulationState.COMPLETED, e.getStatus());
        for (Process p : e.getProcesses()) {
            assertEquals(p.getPid() + " state", ProcessState.COMPLETED, p.getState());
            assertEquals(p.getPid() + " remaining", 0, p.getRemainingTime());
        }
        int before = e.getCurrentTime();
        e.advanceOneTimeUnit();
        assertEquals("no progress after completion", before, e.getCurrentTime());
        SimulationEngine stopped = new SimulationEngine(SampleWorkloads.basic(), cfg(SchedulingAlgorithmType.FCFS));
        stopped.stepSimulation();
        stopped.stopSimulation();
        assertEquals("stopped", SimulationState.STOPPED, stopped.getStatus());
        assertEquals("first event is an arrival", SimulationEventType.PROCESS_ARRIVAL, e.getEvents().get(0).getType());
    }

    private static void comparison() {
        TestUtil.section("Comparison / what-if");
        AlgorithmComparisonService svc = new AlgorithmComparisonService();
        List<SimulationResult> results = svc.compareAlgorithms(SampleWorkloads.basic(), new SimulationConfig(),
                Arrays.asList(SchedulingAlgorithmType.FCFS, SchedulingAlgorithmType.SJF, SchedulingAlgorithmType.SRTF));
        assertEquals("three results", 3, results.size());
        assertEquals("FCFS label", "FCFS", results.get(0).getLabel());
        assertClose("FCFS avg WT", 10.0 / 3, results.get(0).getMetrics().getAvgWaitingTime());
        assertClose("SJF avg WT", 3.0, results.get(1).getMetrics().getAvgWaitingTime());
        assertClose("SRTF avg WT", 7.0 / 3, results.get(2).getMetrics().getAvgWaitingTime());

        List<SimulationResult> quanta = svc.compareQuantums(SampleWorkloads.roundRobin(), new SimulationConfig(), new int[] {1, 2, 4, 8});
        assertEquals("four what-if runs", 4, quanta.size());
        assertEquals("what-if label", "RR q=4", quanta.get(2).getLabel());
        assertEquals("q=8 gantt", "P1:0-5 P2:5-9 P3:9-12", gantt(quanta.get(3).getSegments()));
        check("smaller quantum -> more context switches",
                quanta.get(0).getMetrics().getContextSwitches() > quanta.get(3).getMetrics().getContextSwitches());

        Map<String, SimulationConfig> map = new LinkedHashMap<>();
        map.put("custom", new SimulationConfig());
        assertEquals("general comparison", 1, svc.compareConfigurations(SampleWorkloads.basic(), map).size());
        TestUtil.expectException("empty workload", () -> svc.compareAlgorithms(new ArrayList<>(), new SimulationConfig(),
                Arrays.asList(SchedulingAlgorithmType.FCFS)), "at least one process");
    }
}
