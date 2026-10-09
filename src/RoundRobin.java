import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoundRobin implements Scheduler {

    private final int timeQuantum;

    public RoundRobin(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException(
                "Time quantum must be greater than zero."
            );
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getName() {
        return "Round Robin (Quantum: " + timeQuantum + ")";
    }

    @Override
    public List<Process> schedule(List<Process> processes) {
        if (processes == null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }

        List<Process> jobs = new ArrayList<>();
        for (Process process : processes) {
            if (process == null) {
                throw new IllegalArgumentException(
                    "Process list cannot contain null entries."
                );
            }
            if (process.getArrivalTime() < 0) {
                throw new IllegalArgumentException(
                    "Process arrival times cannot be negative."
                );
            }
            if (process.getBurstTime() <= 0) {
                throw new IllegalArgumentException(
                    "Process burst times must be greater than zero."
                );
            }
            jobs.add(process.copy());
        }

        if (jobs.isEmpty()) {
            System.out.println("No processes available.");
            return jobs;
        }

        // Stable sorting preserves input order when arrival times are equal.
        jobs.sort(Comparator.comparingInt(Process::getArrivalTime));

        CircularQueue<Process> ready = new CircularQueue<>(jobs.size());
        List<Process> completionOrder = new ArrayList<>();
        List<String> intervals = new ArrayList<>();

        int nextArrival = 0;
        int completed = 0;
        int currentTime = 0;
        int processCount = jobs.size();

        while (completed < processCount) {
            while (nextArrival < processCount
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.enqueue(jobs.get(nextArrival));
                nextArrival++;
            }

            if (ready.isEmpty()) {
                int nextTime = jobs.get(nextArrival).getArrivalTime();
                intervals.add(currentTime + "-" + nextTime + ": Idle");
                currentTime = nextTime;
                continue;
            }

            Process process = ready.dequeue();
            if (process.getFirstStartTime() == -1) {
                process.setFirstStartTime(currentTime);
            }

            int startTime = currentTime;
            int runTime = Math.min(timeQuantum, process.getRemainingTime());
            currentTime = Math.addExact(currentTime, runTime);
            process.setRemainingTime(process.getRemainingTime() - runTime);
            intervals.add(
                startTime + "-" + currentTime + ": " + process.getPid()
            );

            // Enqueue arrivals during this time slice before re-queuing the
            // unfinished process, preserving ready-queue arrival order.
            while (nextArrival < processCount
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.enqueue(jobs.get(nextArrival));
                nextArrival++;
            }

            if (process.getRemainingTime() > 0) {
                ready.enqueue(process);
            } else {
                process.setCompletionTime(currentTime);
                Metrics.calculateProcessMetrics(process);
                completionOrder.add(process);
                completed++;
            }
        }

        System.out.println("\nScheduling Algorithm: " + getName());
        System.out.print("Completion Order: ");
        for (Process process : completionOrder) {
            System.out.print(process.getPid() + " ");
        }
        System.out.println();

        System.out.println("Gantt Chart (start-end: process):");
        System.out.println(String.join(" | ", intervals));
        Metrics.displayMetrics(completionOrder);

        return completionOrder;
    }
}