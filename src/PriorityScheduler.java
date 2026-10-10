
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityScheduler{

    // Lower priority number means higher priority
    private static final Comparator<Process> PRIORITY_ORDER =
        Comparator.comparingInt(Process::getPriority)
                  .thenComparingInt(Process::getArrivalTime)
                  .thenComparing(Process::getPid);

    
    private static String getName() {
        return "Priority Scheduling";
    }

    
    public static List<Process> schedule(List<Process> processes) {

        List<Process> jobs = new ArrayList<>();

        // Create independent process records
        for (Process p : processes) {
            jobs.add(p.copy());
        }

        if (jobs.isEmpty()) {
            System.out.println("No processes available.");
            return jobs;
        }

        // Sort processes by arrival time
        jobs.sort(
            Comparator.comparingInt(Process::getArrivalTime)
                      .thenComparing(Process::getPid)
        );

        // Heap selects the highest-priority ready process
        MinHeap ready = new MinHeap(
            Math.max(1, jobs.size()), PRIORITY_ORDER
        );

        List<Process> executionOrder = new ArrayList<>();

        int next = 0;
        int completed = 0;
        int currentTime = 0;
        int n = jobs.size();

        while (completed < n) {

            // Add all processes that have arrived
            while (next < n &&
                   jobs.get(next).getArrivalTime() <= currentTime) {

                ready.insert(jobs.get(next));
                next++;
            }

            // If no process is ready, advance to the next arrival
            if (ready.isEmpty()) {
                currentTime = jobs.get(next).getArrivalTime();
                continue;
            }

            // Select the process with the highest priority
            Process p = ready.removeMin();

            p.setFirstStartTime(currentTime);
            currentTime += p.getBurstTime();

            p.setRemainingTime(0);
            p.setCompletionTime(currentTime);

            // Calculate TAT, WT, and RT
            Metrics.calculateProcessMetrics(p);

            executionOrder.add(p);
            completed++;
        }

        // Display scheduling results
        System.out.println("\nScheduling Algorithm: " + getName());

        System.out.print("Execution Order: ");
        for (Process p : executionOrder) {
            System.out.print(p.getPid() + " ");
        }
        System.out.println();

        Metrics.displayMetrics(executionOrder);

        return executionOrder;
    }
}