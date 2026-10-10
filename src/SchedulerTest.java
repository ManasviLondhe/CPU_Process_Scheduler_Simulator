import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SchedulerTest {
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Starting scheduler validation tests...\n");

        testProcessCreation();
        testDuplicatePidRejected();
        testEmptyScheduler();
        testFCFSOrder();
        testSJFOrder();
        testPriorityOrder();
        testRoundRobinExecution();
        testMetricsAreValid();
        testCompareAllAlgorithms();

        if (failed == 0) {
            System.out.println("\nALL TESTS PASSED");
        } else {
            System.out.println("\nFAILED TESTS: " + failed);
        }
    }

    private static void testProcessCreation() {
        checkNoThrow("Process creation with valid values",
                () -> new Process("P1", 0, 5, 2));

        checkThrows("Negative arrival time rejected",
                () -> new Process("P2", -1, 5, 2), IllegalArgumentException.class);

        checkThrows("Zero burst time rejected",
                () -> new Process("P3", 0, 0, 2), IllegalArgumentException.class);

        checkThrows("Negative priority rejected",
                () -> new Process("P4", 0, 5, -1), IllegalArgumentException.class);
    }

    private static void testDuplicatePidRejected() {
        ProcessManager manager = new ProcessManager();
        Process p1 = new Process("P1", 0, 5, 2);
        Process p2 = new Process("P1", 1, 4, 1);

        checkTrue("First process added", manager.addProcess(p1));
        checkFalse("Duplicate PID rejected", manager.addProcess(p2));
    }

    private static void testEmptyScheduler() {
        ProcessManager manager = new ProcessManager();

        checkTrue("No processes available initially", manager.isEmpty());
        checkNoThrow("FCFS should handle empty list", () -> Scheduler.schedule(1, manager.getProcesses(), null));
        checkNoThrow("SJF should handle empty list", () -> Scheduler.schedule(2, manager.getProcesses(), null));
        checkNoThrow("Priority should handle empty list", () -> Scheduler.schedule(3, manager.getProcesses(), null));
        checkNoThrow("Round Robin should handle empty list", () -> Scheduler.schedule(4, manager.getProcesses(), 2));
    }

    private static void testFCFSOrder() {
        List<Process> jobs = buildSampleSet();
        Metrics.SchedulingResult result = Scheduler.schedule(1, jobs, null);

        checkEquals("FCFS order size", jobs.size(), result.getExecutionOrder().size());
        checkStringEquals("FCFS first PID", "P1", result.getExecutionOrder().get(0).getPid());
        checkStringEquals("FCFS last PID", "P4", result.getExecutionOrder().get(3).getPid());
    }

    private static void testSJFOrder() {
        List<Process> jobs = buildSampleSet();
        Metrics.SchedulingResult result = Scheduler.schedule(2, jobs, null);

        checkEquals("SJF order size", jobs.size(), result.getExecutionOrder().size());
        checkStringEquals("SJF first PID", "P1", result.getExecutionOrder().get(0).getPid());
        checkStringEquals("SJF second PID", "P2", result.getExecutionOrder().get(1).getPid());
    }

    private static void testPriorityOrder() {
        List<Process> jobs = buildSampleSet();
        Metrics.SchedulingResult result = Scheduler.schedule(3, jobs, null);

        checkEquals("Priority order size", jobs.size(), result.getExecutionOrder().size());
        checkStringEquals("Priority first PID", "P1", result.getExecutionOrder().get(0).getPid());
        checkStringEquals("Priority second PID", "P2", result.getExecutionOrder().get(1).getPid());
    }

    private static void testRoundRobinExecution() {
        List<Process> jobs = buildSampleSet();
        Metrics.SchedulingResult result = Scheduler.schedule(4, jobs, 2);

        checkEquals("RR order size", jobs.size(), result.getExecutionOrder().size());
        checkTrue("RR total elapsed time positive", result.getElapsedTime() > 0);
        checkTrue("RR busy time matches burst sum", result.getBusyTime() == 22);
        checkTrue("RR average turnaround non-negative",
                result.getAverageTurnaroundTime() >= 0);
        checkTrue("RR average waiting non-negative",
                result.getAverageWaitingTime() >= 0);
        checkTrue("RR average response non-negative",
                result.getAverageResponseTime() >= 0);
    }

    private static void testMetricsAreValid() {
        List<Process> jobs = buildSampleSet();
        Metrics.SchedulingResult result = Scheduler.schedule(1, jobs, null);

        checkTrue("CPU utilization in range",
                result.getCpuUtilization() >= 0 && result.getCpuUtilization() <= 100);
        checkTrue("Execution order contains all PIDs",
                containsAllPids(result, "P1", "P2", "P3", "P4"));
        checkTrue("Average values are finite",
                Double.isFinite(result.getAverageTurnaroundTime())
                        && Double.isFinite(result.getAverageWaitingTime())
                        && Double.isFinite(result.getAverageResponseTime()));
    }

    private static void testCompareAllAlgorithms() {
        List<Process> jobs = buildSampleSet();
        List<Metrics.SchedulingResult> all = Scheduler.scheduleAll(jobs, 2);

        checkEquals("All algorithms count", 4, all.size());
        for (Metrics.SchedulingResult result : all) {
            checkTrue("Comparison result not null", result != null);
            checkEquals("Execution order count", jobs.size(), result.getExecutionOrder().size());
        }
    }

    private static List<Process> buildSampleSet() {
        return Arrays.asList(
                new Process("P1", 0, 5, 2),
                new Process("P2", 1, 3, 1),
                new Process("P3", 2, 8, 3),
                new Process("P4", 3, 6, 2)
        );
    }

    private static boolean runScheduleAndCatch(int algorithm, List<Process> processes, Integer quantum) {
        try {
            Scheduler.schedule(algorithm, processes, quantum);
            return false;
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }

    private static boolean containsAllPids(Metrics.SchedulingResult result, String... pids) {
        List<String> ids = new ArrayList<>();
        for (Process p : result.getExecutionOrder()) {
            ids.add(p.getPid());
        }

        for (String pid : pids) {
            if (!ids.contains(pid)) {
                return false;
            }
        }
        return true;
    }

    private static void checkTrue(String label, boolean condition) {
        if (!condition) {
            failed++;
            System.out.println("[FAIL] " + label);
        } else {
            System.out.println("[PASS] " + label);
        }
    }

    private static void checkFalse(String label, boolean condition) {
        checkTrue(label, !condition);
    }

    private static void checkEquals(String label, int expected, int actual) {
        if (expected != actual) {
            failed++;
            System.out.println("[FAIL] " + label + " -> expected " + expected + ", got " + actual);
        } else {
            System.out.println("[PASS] " + label);
        }
    }

    private static void checkStringEquals(String label, String expected, String actual) {
        if (!expected.equals(actual)) {
            failed++;
            System.out.println("[FAIL] " + label + " -> expected '" + expected + "', got '" + actual + "'");
        } else {
            System.out.println("[PASS] " + label);
        }
    }

    private static void checkNoThrow(String label, Runnable action) {
        try {
            action.run();
            System.out.println("[PASS] " + label);
        } catch (Exception ex) {
            failed++;
            System.out.println("[FAIL] " + label + " -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private static void checkThrows(String label, Runnable action, Class<? extends Exception> expected) {
        try {
            action.run();
            failed++;
            System.out.println("[FAIL] " + label + " -> expected " + expected.getSimpleName() + " but none was thrown.");
        } catch (Exception ex) {
            if (expected.isInstance(ex)) {
                System.out.println("[PASS] " + label);
            } else {
                failed++;
                System.out.println("[FAIL] " + label + " -> expected " + expected.getSimpleName() + ", got " + ex.getClass().getSimpleName());
            }
        }
    }
}
