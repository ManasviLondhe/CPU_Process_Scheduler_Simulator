package tests;

import static tests.TestUtil.assertClose;
import static tests.TestUtil.assertEquals;
import static tests.TestUtil.check;
import static tests.TestUtil.expectException;

import java.util.ArrayList;
import java.util.List;

import generator.ProcessGenerator;
import generator.WorkloadPreset;
import metrics.MetricsCalculator;
import model.MetricsSummary;
import model.Process;
import model.ProcessState;
import model.ScheduleSegment;
import model.SchedulingAlgorithmType;
import model.SegmentType;
import model.SimulationConfig;
import validation.InputValidator;

/** Tests for MetricsCalculator, InputValidator and ProcessGenerator. */
public final class MetricsAndValidationTests {
    private MetricsAndValidationTests() {
    }

    public static void run() {
        TestUtil.section("MetricsCalculator");
        Process p = new Process("P1", 2, 4);
        p.setFirstStartTime(5);
        p.setCompletionTime(12);
        p.setState(ProcessState.COMPLETED);
        MetricsCalculator.applyProcessMetrics(p);
        assertEquals("TAT = CT - AT", 10, p.getTurnaroundTime());
        assertEquals("WT = TAT - BT", 6, p.getWaitingTime());
        assertEquals("RT = start - AT", 3, p.getResponseTime());

        List<Process> ps = new ArrayList<>();
        ps.add(p);
        List<ScheduleSegment> segs = new ArrayList<>();
        segs.add(new ScheduleSegment(SegmentType.IDLE, null, 0, 2));
        segs.add(new ScheduleSegment(SegmentType.PROCESS, "P1", 2, 6));
        segs.add(new ScheduleSegment(SegmentType.CONTEXT_SWITCH, null, 6, 8));
        MetricsSummary m = MetricsCalculator.calculate(ps, segs, 8, 1, 2);
        assertClose("utilization", 50.0, m.getCpuUtilization());
        assertClose("throughput", 1.0 / 8, m.getThroughput());
        assertEquals("idle", 2, m.getIdleTime());
        assertEquals("busy", 4, m.getBusyTime());
        MetricsSummary empty = MetricsCalculator.calculate(new ArrayList<>(), new ArrayList<>(), 0, 0, 0);
        assertClose("empty avg is 0, no division by zero", 0, empty.getAvgWaitingTime());

        TestUtil.section("InputValidator");
        List<Process> existing = new ArrayList<>();
        existing.add(new Process("P1", 0, 5));
        expectException("empty pid", () -> InputValidator.validateProcess("", 0, 1, 1, 1, "", existing, null), "PID cannot be empty");
        expectException("duplicate pid", () -> InputValidator.validateProcess("p1", 0, 1, 1, 1, "", existing, null), "PID already exists");
        expectException("negative arrival", () -> InputValidator.validateProcess("P2", -1, 1, 1, 1, "", existing, null), "Arrival time must be 0 or greater");
        expectException("zero burst", () -> InputValidator.validateProcess("P2", 0, 0, 1, 1, "", existing, null), "Burst time must be greater than 0");
        expectException("negative burst", () -> InputValidator.validateProcess("P2", 0, -3, 1, 1, "", existing, null), "Burst time must be greater than 0");
        expectException("bad priority", () -> InputValidator.validateProcess("P2", 0, 1, -1, 1, "", existing, null), "Priority");
        expectException("bad queue", () -> InputValidator.validateProcess("P2", 0, 1, 1, 9, "", existing, null), "Queue");
        expectException("bad pid chars", () -> InputValidator.validateProcess("P 2,", 0, 1, 1, 1, "", existing, null), "PID may contain");
        InputValidator.validateProcess("P1", 0, 7, 1, 1, "", existing, "P1");
        check("editing keeps own pid", true);
        expectException("not a number", () -> InputValidator.parseInt("abc", "Arrival time"), "whole number");
        expectException("no processes", () -> InputValidator.validateWorkload(new ArrayList<>()), "Please add at least one process");
        SimulationConfig c = new SimulationConfig();
        c.setAlgorithm(SchedulingAlgorithmType.ROUND_ROBIN);
        c.setTimeQuantum(0);
        expectException("quantum 0", () -> InputValidator.validateConfig(c), "Time quantum must be greater than 0");

        TestUtil.section("ProcessGenerator");
        ProcessGenerator gen = new ProcessGenerator();
        List<Process> a = gen.generate(20, 0, 10, 2, 9, 1, 5, 3, 42L, 1);
        List<Process> b = gen.generate(20, 0, 10, 2, 9, 1, 5, 3, 42L, 1);
        assertEquals("count", 20, a.size());
        boolean same = true;
        boolean inRange = true;
        for (int i = 0; i < a.size(); i++) {
            same &= a.get(i).getBurstTime() == b.get(i).getBurstTime() && a.get(i).getArrivalTime() == b.get(i).getArrivalTime();
            inRange &= a.get(i).getBurstTime() >= 2 && a.get(i).getBurstTime() <= 9
                    && a.get(i).getArrivalTime() <= 10 && a.get(i).getPriority() >= 1 && a.get(i).getPriority() <= 5;
        }
        check("same seed -> same workload", same);
        check("values inside requested ranges", inRange);
        for (WorkloadPreset preset : WorkloadPreset.values()) {
            check("preset " + preset + " generates", !gen.generatePreset(preset, 1L, 1).isEmpty());
        }
        expectException("too many processes", () -> gen.generate(101, 0, 1, 1, 2, 1, 2, 3, null, 1), "between 1 and 100");
        expectException("bad burst range", () -> gen.generate(3, 0, 1, 5, 2, 1, 2, 3, null, 1), "Burst range");
    }
}
