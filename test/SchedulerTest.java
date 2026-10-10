import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class SchedulerTest {

    private static int passed;

    private SchedulerTest() {
    }

    public static void main(String[] args) {
        testProcessManager();
        testDataStructures();
        testSchedulingResults();
        System.out.println("PASS: " + passed + " checks");
    }

    private static void testProcessManager() {
        ProcessManager manager = new ProcessManager();
        check(manager.addProcess(new Process("P1", 0, 3, 1)),
                "process can be added");
        check(!manager.addProcess(new Process("p1", 2, 4, 2)),
                "duplicate PID is rejected case-insensitively");
        check(manager.searchProcess("p1") != null,
                "PID search is case-insensitive");
        check(manager.deleteProcess("P1"), "process can be deleted");
        check(manager.isEmpty(), "manager is empty after deletion");
    }

    private static void testDataStructures() {
        CircularQueue<Integer> queue = new CircularQueue<>(2);
        queue.enqueue(1);
        queue.enqueue(2);
        check(queue.dequeue() == 1, "queue preserves FIFO order");
        queue.enqueue(3);
        check(queue.dequeue() == 2 && queue.dequeue() == 3,
                "queue supports wraparound");

        MinHeap heap = new MinHeap(1,
                Comparator.comparingInt(Process::getBurstTime));
        heap.insert(new Process("P1", 0, 5, 1));
        heap.insert(new Process("P2", 0, 2, 1));
        check("P2".equals(heap.removeMin().getPid()),
                "heap removes the minimum process");
    }

    private static void testSchedulingResults() {
        List<Process> processes = Arrays.asList(
                new Process("P1", 0, 5, 2),
                new Process("P2", 1, 3, 1),
                new Process("P3", 2, 2, 0));

        Metrics.SchedulingResult fcfs = Scheduler.schedule(
                1, processes, null);
        check(fcfs.getExecutionOrder().size() == 3,
                "FCFS returns every process");
        check(fcfs.getAverageWaitingTime() > 0,
                "FCFS calculates waiting time");

        Metrics.SchedulingResult priority = Scheduler.schedule(
                3, processes, null);
        check("P1".equals(priority.getExecutionOrder().get(0).getPid()),
                "priority scheduling does not preempt a running process");

        Metrics.SchedulingResult roundRobin = Scheduler.schedule(
                4, processes, 2);
        check(roundRobin.getIntervals().size() > processes.size(),
                "Round Robin records multiple time slices");

        Metrics.SchedulingResult idle = Scheduler.schedule(
                1, Arrays.asList(new Process("P4", 3, 2, 1)), null);
        check(idle.getCpuUtilization() < 100.0,
                "idle time reduces CPU utilization");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("FAIL: " + description);
        }
        passed++;
    }
}
