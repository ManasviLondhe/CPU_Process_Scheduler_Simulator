
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityScheduler implements Scheduler {

    private static final Comparator<Process> PRIORITY_ORDER =
        Comparator.comparingInt((Process p) -> p.priority)
                  .thenComparingInt(p -> p.arrivalTime)
                  .thenComparingInt(p -> p.pid);

    @Override
    public String getName() {
        return "Priority Scheduling";
    }

    @Override
    public List<Process> schedule(List<Process> processes) {
        List<Process> jobs = new ArrayList<>();

        // Each simulation starts with fresh process records.
        for (Process p : processes) {
            jobs.add(new Process(
                p.pid, p.arrivalTime, p.burstTime, p.priority
            ));
        }

        jobs.sort(
            Comparator.comparingInt((Process p) -> p.arrivalTime)
                      .thenComparingInt(p -> p.pid)
        );

        MinHeap ready = new MinHeap(
            Math.max(1, jobs.size()), PRIORITY_ORDER
        );

        List<Process> executionOrder = new ArrayList<>();

        int next = 0;
        int completed = 0;
        int currentTime = 0;
        int n = jobs.size();

        while (completed < n) {

            // Add every process that has arrived.
            while (next < n &&
                   jobs.get(next).arrivalTime <= currentTime) {
                ready.insert(jobs.get(next));
                next++;
            }

            if (ready.isEmpty()) {
                currentTime = jobs.get(next).arrivalTime;
                continue;
            }

            Process p = ready.removeMin();

            p.firstStartTime = currentTime;
            currentTime += p.burstTime;

            p.remainingTime = 0;
            p.completionTime = currentTime;

            p.turnaroundTime =
                p.completionTime - p.arrivalTime;

            p.waitingTime =
                p.turnaroundTime - p.burstTime;

            p.responseTime =
                p.firstStartTime - p.arrivalTime;

            executionOrder.add(p);
            completed++;
        }

        return executionOrder;
    }
}
