
import java.util.List;

public class Metrics {

    // Calculate metrics for one process
    public static void calculateProcessMetrics(Process p) {

        // Turnaround Time = Completion Time - Arrival Time
        p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
        p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
        p.setResponseTime(p.getFirstStartTime() - p.getArrivalTime());
    }

    // Display metrics for all processes
    public static void displayMetrics(List<Process> processes) {

        if (processes.isEmpty()) {
            System.out.println("No process metrics available.");
            return;
        }

        double totalTAT = 0;
        double totalWT = 0;
        double totalRT = 0;

        System.out.println("\nProcess Performance Metrics");
        System.out.println("---------------------------------------------------------------");

        System.out.printf("%-8s %-8s %-8s %-8s %-8s %-8s%n",
                "PID", "CT", "TAT", "WT", "RT", "Burst");

        System.out.println("---------------------------------------------------------------");

        for (Process p : processes) {

            System.out.printf("%-8s %-8d %-8d %-8d %-8d %-8d%n",
                    p.getPid(),
                    p.getCompletionTime(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime(),
                    p.getResponseTime(),
                    p.getBurstTime());

            totalTAT += p.getTurnaroundTime();
            totalWT += p.getWaitingTime();
            totalRT += p.getResponseTime();
        }

        System.out.println("---------------------------------------------------------------");

        int n = processes.size();

        System.out.printf("Average Turnaround Time : %.2f%n", totalTAT / n);
        System.out.printf("Average Waiting Time    : %.2f%n", totalWT / n);
        System.out.printf("Average Response Time   : %.2f%n", totalRT / n);
    }

    // Calculate CPU utilization
    public static double calculateCPUUtilization(
            int busyTime, int totalElapsedTime) {

        if (totalElapsedTime <= 0) {
            return 0.0;
        }

        return (busyTime * 100.0) / totalElapsedTime;
    }
}