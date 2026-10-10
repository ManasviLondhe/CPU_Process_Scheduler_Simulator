import java.util.ArrayList;
import java.util.List;

public final class Scheduler {

    private Scheduler() {
    }

    public static Metrics.SchedulingResult schedule(
            int algorithm, List<Process> processes, Integer timeQuantum) {
        switch (algorithm) {
            case 1:
                return FCFS.schedule(processes);
            case 2:
                return SJF.schedule(processes);
            case 3:
                return PriorityScheduler.schedule(processes);
            case 4:
                if (timeQuantum == null || timeQuantum <= 0) {
                    throw new IllegalArgumentException(
                        "Time quantum must be greater than zero."
                    );
                }
                return new RoundRobin(timeQuantum).schedule(processes);
            default:
                throw new IllegalArgumentException(
                    "Unknown scheduling algorithm: " + algorithm
                );
        }
    }

    public static List<Metrics.SchedulingResult> scheduleAll(
            List<Process> processes, int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException(
                "Time quantum must be greater than zero."
            );
        }

        List<Metrics.SchedulingResult> results = new ArrayList<>(4);
        results.add(schedule(1, processes, null));
        results.add(schedule(2, processes, null));
        results.add(schedule(3, processes, null));
        results.add(schedule(4, processes, timeQuantum));
        return results;
    }
}
