import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FCFS {

    private FCFS() {
    }

    public static Metrics.SchedulingResult schedule(
            List<Process> processes) {
        List<Process> jobs = Metrics.copyAndValidateProcesses(processes);
        jobs.sort(Comparator.comparingInt(Process::getArrivalTime));

        List<Process> executionOrder = new ArrayList<>(jobs.size());
        List<Metrics.Interval> intervals = new ArrayList<>();
        int currentTime = 0;

        for (Process process : jobs) {
            int arrivalTime = process.getArrivalTime();
            if (arrivalTime > currentTime) {
                intervals.add(new Metrics.Interval(
                        currentTime, arrivalTime, null));
                currentTime = arrivalTime;
            }

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
                "First Come First Served (FCFS)",
                executionOrder,
                intervals,
                0,
                currentTime
        );
    }
}
