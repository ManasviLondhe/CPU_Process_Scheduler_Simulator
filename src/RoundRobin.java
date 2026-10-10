import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RoundRobin {

    private final int timeQuantum;

    public RoundRobin(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException(
                "Time quantum must be greater than zero."
            );
        }
        this.timeQuantum = timeQuantum;
    }

    public String getName() {
        return "Round Robin (quantum: " + timeQuantum + ")";
    }

    public Metrics.SchedulingResult schedule(List<Process> processes) {
        List<Process> jobs = Metrics.copyAndValidateProcesses(processes);
        jobs.sort(Comparator.comparingInt(Process::getArrivalTime));

        if (jobs.isEmpty()) {
            return Metrics.createResult(
                    getName(), jobs, new ArrayList<>(), 0, 0);
        }

        CircularQueue<Process> ready = new CircularQueue<>(jobs.size());
        List<Process> firstExecutionOrder = new ArrayList<>(jobs.size());
        List<Metrics.Interval> intervals = new ArrayList<>();
        int nextArrival = 0;
        int currentTime = 0;
        int completed = 0;

        while (completed < jobs.size()) {
            while (nextArrival < jobs.size()
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.enqueue(jobs.get(nextArrival++));
            }

            if (ready.isEmpty()) {
                int nextTime = jobs.get(nextArrival).getArrivalTime();
                intervals.add(new Metrics.Interval(currentTime, nextTime, null));
                currentTime = nextTime;
                continue;
            }

            Process process = ready.dequeue();
            boolean firstRun = process.getFirstStartTime() == -1;
            if (firstRun) {
                process.setFirstStartTime(currentTime);
                firstExecutionOrder.add(process);
            }

            int startTime = currentTime;
            int runTime = Math.min(
                    timeQuantum, process.getRemainingTime());
            currentTime = Math.addExact(currentTime, runTime);
            process.setRemainingTime(process.getRemainingTime() - runTime);
            intervals.add(new Metrics.Interval(
                    startTime, currentTime, process.getPid()));

            while (nextArrival < jobs.size()
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.enqueue(jobs.get(nextArrival++));
            }

            if (process.getRemainingTime() > 0) {
                ready.enqueue(process);
            } else {
                process.setCompletionTime(currentTime);
                completed++;
            }
        }

        return Metrics.createResult(
                getName(),
                firstExecutionOrder,
                intervals,
                0,
                currentTime
        );
    }
}
