import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PriorityScheduler {

    // Non-preemptive scheduling; a lower number represents higher priority.
    private static final Comparator<Process> READY_ORDER =
        Comparator.comparingInt(Process::getPriority)
                  .thenComparingInt(Process::getArrivalTime)
                  .thenComparing(Process::getPid);

    private PriorityScheduler() {
    }

    public static Metrics.SchedulingResult schedule(
            List<Process> processes) {
        List<Process> jobs = Metrics.copyAndValidateProcesses(processes);
        jobs.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        MinHeap ready = new MinHeap(Math.max(1, jobs.size()), READY_ORDER);
        List<Process> executionOrder = new ArrayList<>(jobs.size());
        List<Metrics.Interval> intervals = new ArrayList<>();
        int nextArrival = 0;
        int currentTime = 0;

        while (executionOrder.size() < jobs.size()) {
            while (nextArrival < jobs.size()
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.insert(jobs.get(nextArrival++));
            }

            if (ready.isEmpty()) {
                int nextTime = jobs.get(nextArrival).getArrivalTime();
                intervals.add(new Metrics.Interval(currentTime, nextTime, null));
                currentTime = nextTime;
                continue;
            }

            Process process = ready.removeMin();
            process.setFirstStartTime(currentTime);
            int finishTime = Math.addExact(
                    currentTime, process.getBurstTime());
            process.setRemainingTime(0);
            process.setCompletionTime(finishTime);
            intervals.add(new Metrics.Interval(
                    currentTime, finishTime, process.getPid()));
            executionOrder.add(process);
            currentTime = finishTime;
        }

        return Metrics.createResult(
                "Priority Scheduling (non-preemptive; lower number = higher priority)",
                executionOrder,
                intervals,
                0,
                currentTime
        );
    }
}
