import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFS {

    public static void schedule(List<Process> processes) {
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
            jobs.add(process.copy());
        }

        if (jobs.isEmpty()) {
            System.out.println("No processes available.");
            return;
        }

        // List sorting is stable, so processes with equal arrival times keep
        // their original input order.
        jobs.sort(Comparator.comparingInt(Process::getArrivalTime));

        List<Process> executionOrder = new ArrayList<>();
        List<String> intervals = new ArrayList<>();
        int currentTime = 0;

        for (Process process : jobs) {
            int arrivalTime = process.getArrivalTime();
            if (arrivalTime < 0) {
                throw new IllegalArgumentException(
                    "Process arrival times cannot be negative."
                );
            }
            if (process.getBurstTime() <= 0) {
                throw new IllegalArgumentException(
                    "Process burst times must be greater than zero."
                );
            }

            if (arrivalTime > currentTime) {
                intervals.add(
                    currentTime + "-" + arrivalTime + ": Idle"
                );
                currentTime = arrivalTime;
            }

            int startTime = currentTime;
            process.setFirstStartTime(startTime);
            currentTime += process.getBurstTime();
            process.setRemainingTime(0);
            process.setCompletionTime(currentTime);
            Metrics.calculateProcessMetrics(process);

            intervals.add(
                startTime + "-" + currentTime + ": " + process.getPid()
            );
            executionOrder.add(process);
        }

        System.out.println("\nScheduling Algorithm: First Come First Served");
        System.out.print("Execution Order: ");
        for (Process process : executionOrder) {
            System.out.print(process.getPid() + " ");
        }
        System.out.println();

        System.out.println("Gantt Chart (start-end: process):");
        System.out.println(String.join(" | ", intervals));
        Metrics.displayMetrics(executionOrder);
    }
}