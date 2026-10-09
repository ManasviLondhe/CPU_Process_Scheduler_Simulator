
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJF implements Scheduler {

    // Shortest burst time first
    private static final Comparator<Process> SJF_ORDER =
        Comparator.comparingInt(Process::getBurstTime)
                  .thenComparingInt(Process::getArrivalTime)
                  .thenComparing(Process::getPid);

    @Override
    public String getName() {
        return "Shortest Job First";
    }

    @Override
    public List<Process> schedule(List<Process> processes) {

        List<Process> jobs = new ArrayList<>();

        // Create independent process records
        for (Process p : processes) {
            jobs.add(p.copy());
        }

        if (jobs.isEmpty()) {
            System.out.println("No processes available.");
            return jobs;
        }

        // Sort by arrival time
        jobs.sort(
            Comparator.comparingInt(Process::getArrivalTime)
                      .thenComparing(Process::getPid)
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

            // Add all arrived processes to the heap
            while (next < n &&
                   jobs.get(next).getArrivalTime() <= currentTime) {

                ready.insert(jobs.get(next));
                next++;
            }

            // CPU is idle until the next process arrives
            if (ready.isEmpty()) {
                currentTime = jobs.get(next).getArrivalTime();
                continue;
            }

            // Select the process with the shortest burst time
            Process p = ready.removeMin();

            p.setFirstStartTime(currentTime);

            currentTime += p.getBurstTime();

            p.setRemainingTime(0);
            p.setCompletionTime(currentTime);

            // Calculate performance metrics
            Metrics.calculateProcessMetrics(p);

            executionOrder.add(p);
            completed++;
        }

        // Display the execution order and performance metrics
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