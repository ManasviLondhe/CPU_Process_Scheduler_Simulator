
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJF implements Scheduler {

    private static final Comparator<Process> SJF_ORDER =
        Comparator.comparingInt((Process p) -> p.burstTime)
                  .thenComparingInt(p -> p.arrivalTime)
                  .thenComparingInt(p -> p.pid);

    @Override
    public String getName() {
        return "Shortest Job First";
    }

    @Override
    public List<Process> schedule(List<Process> processes) {
        List<Process> jobs = new ArrayList<>();

        // Create independent records for this simulation.
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
            Math.max(1, jobs.size()), SJF_ORDER
        );

        List<Process> executionOrder = new ArrayList<>();

        int next = 0;
        int completed = 0;
        int currentTime = 0;
        int n = jobs.size();

        while (completed < n) {

            // Only processes that have arrived become ready.
            while (next < n &&
                   jobs.get(next).arrivalTime <= currentTime) {
                ready.insert(jobs.get(next));
                next++;
            }

            // No ready job: advance time to the next arrival.
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
