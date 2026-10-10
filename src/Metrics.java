import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Metrics {

    private Metrics() {
    }

    public static final class Interval {
        private final int startTime;
        private final int endTime;
        private final String processId;

        public Interval(int startTime, int endTime, String processId) {
            if (startTime < 0 || endTime <= startTime) {
                throw new IllegalArgumentException(
                    "An interval must have a non-negative start and positive duration."
                );
            }
            if (processId != null && processId.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "An executing interval must identify its process."
                );
            }
            this.startTime = startTime;
            this.endTime = endTime;
            this.processId = processId;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public String getProcessId() {
            return processId;
        }

        public boolean isIdle() {
            return processId == null;
        }

        @Override
        public String toString() {
            return startTime + "-" + endTime + ": "
                    + (isIdle() ? "Idle" : processId);
        }
    }

    public static final class SchedulingResult {
        private final String algorithmName;
        private final List<Process> executionOrder;
        private final List<Interval> intervals;
        private final double averageTurnaroundTime;
        private final double averageWaitingTime;
        private final double averageResponseTime;
        private final long busyTime;
        private final long elapsedTime;
        private final double cpuUtilization;

        private SchedulingResult(
                String algorithmName,
                List<Process> executionOrder,
                List<Interval> intervals,
                double averageTurnaroundTime,
                double averageWaitingTime,
                double averageResponseTime,
                long busyTime,
                long elapsedTime,
                double cpuUtilization) {
            this.algorithmName = algorithmName;
            this.executionOrder = immutableProcessSnapshots(executionOrder);
            this.intervals = Collections.unmodifiableList(
                    new ArrayList<>(intervals));
            this.averageTurnaroundTime = averageTurnaroundTime;
            this.averageWaitingTime = averageWaitingTime;
            this.averageResponseTime = averageResponseTime;
            this.busyTime = busyTime;
            this.elapsedTime = elapsedTime;
            this.cpuUtilization = cpuUtilization;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public List<Process> getExecutionOrder() {
            return immutableProcessSnapshots(executionOrder);
        }

        public List<Interval> getIntervals() {
            return intervals;
        }

        public double getAverageTurnaroundTime() {
            return averageTurnaroundTime;
        }

        public double getAverageWaitingTime() {
            return averageWaitingTime;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public long getBusyTime() {
            return busyTime;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public double getCpuUtilization() {
            return cpuUtilization;
        }
    }

    public static List<Process> copyAndValidateProcesses(
            List<Process> processes) {
        if (processes == null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }

        List<Process> copies = new ArrayList<>(processes.size());
        Set<String> processIds = new HashSet<>();
        for (Process process : processes) {
            if (process == null) {
                throw new IllegalArgumentException(
                    "Process list cannot contain null entries."
                );
            }
            String processId = normalizePid(process.getPid());
            if (!processIds.add(processId)) {
                throw new IllegalArgumentException(
                    "Process IDs must be unique, ignoring letter case."
                );
            }
            copies.add(process.copy());
        }
        return copies;
    }

    private static List<Process> immutableProcessSnapshots(
            List<Process> processes) {
        List<Process> snapshots = new ArrayList<>(processes.size());
        for (Process process : processes) {
            snapshots.add(process.snapshot());
        }
        return Collections.unmodifiableList(snapshots);
    }

    private static String normalizePid(String processId) {
        return processId.trim().toLowerCase(Locale.ROOT);
    }

    public static void calculateProcessMetrics(Process process) {
        if (process == null) {
            throw new IllegalArgumentException("Process cannot be null.");
        }
        if (process.getFirstStartTime() < process.getArrivalTime()
                || process.getCompletionTime() < process.getFirstStartTime()) {
            throw new IllegalArgumentException(
                "Process must have valid start and completion times."
            );
        }

        int turnaroundTime = process.getCompletionTime()
                - process.getArrivalTime();
        int waitingTime = turnaroundTime - process.getBurstTime();
        int responseTime = process.getFirstStartTime()
                - process.getArrivalTime();
        if (waitingTime < 0 || responseTime < 0) {
            throw new IllegalArgumentException(
                "Calculated process metrics cannot be negative."
            );
        }

        process.setTurnaroundTime(turnaroundTime);
        process.setWaitingTime(waitingTime);
        process.setResponseTime(responseTime);
    }

    public static SchedulingResult createResult(
            String algorithmName,
            List<Process> executionOrder,
            List<Interval> intervals,
            int simulationStart,
            int completionTime) {
        if (algorithmName == null || algorithmName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Algorithm name cannot be empty."
            );
        }
        if (executionOrder == null || intervals == null) {
            throw new IllegalArgumentException(
                "Execution order and intervals cannot be null."
            );
        }
        if (simulationStart < 0 || completionTime < simulationStart) {
            throw new IllegalArgumentException(
                "Invalid simulation time range."
            );
        }

        long totalTurnaround = 0;
        long totalWaiting = 0;
        long totalResponse = 0;
        long busyTime = 0;
        List<Process> resultOrder = new ArrayList<>(executionOrder.size());
        Map<String, Process> processesById = new HashMap<>();
        Map<String, Long> intervalBusyById = new HashMap<>();
        Map<String, Integer> firstIntervalById = new HashMap<>();
        Map<String, Integer> lastIntervalById = new HashMap<>();
        int previousFirstStart = -1;
        for (Process inputProcess : executionOrder) {
            if (inputProcess == null) {
                throw new IllegalArgumentException(
                    "Execution order cannot contain null processes."
                );
            }
            Process process = inputProcess.snapshot();
            resultOrder.add(process);
            String processId = normalizePid(process.getPid());
            if (processesById.put(processId, process) != null) {
                throw new IllegalArgumentException(
                    "Execution order cannot contain duplicate processes."
                );
            }
            if (process.getFirstStartTime() <= previousFirstStart
                    || process.getRemainingTime() != 0) {
                throw new IllegalArgumentException(
                    "Execution order must follow first start, and every process must be complete."
                );
            }
            previousFirstStart = process.getFirstStartTime();
            intervalBusyById.put(processId, 0L);
            calculateProcessMetrics(process);
            totalTurnaround = Math.addExact(
                    totalTurnaround, process.getTurnaroundTime());
            totalWaiting = Math.addExact(
                    totalWaiting, process.getWaitingTime());
            totalResponse = Math.addExact(
                    totalResponse, process.getResponseTime());
            busyTime = Math.addExact(busyTime, process.getBurstTime());
        }

        long intervalBusyTime = 0;
        int expectedStart = simulationStart;
        for (Interval interval : intervals) {
            if (interval == null || interval.getStartTime() != expectedStart) {
                throw new IllegalArgumentException(
                    "Gantt intervals must be non-null, ordered, and contiguous."
                );
            }
            if (!interval.isIdle()) {
                String processId = normalizePid(interval.getProcessId());
                Process process = processesById.get(processId);
                if (process == null
                        || interval.getStartTime() < process.getArrivalTime()
                        || interval.getEndTime() > process.getCompletionTime()) {
                    throw new IllegalArgumentException(
                        "Gantt interval is outside the process execution window."
                    );
                }
                long duration = (long) interval.getEndTime()
                        - interval.getStartTime();
                if (!firstIntervalById.containsKey(processId)) {
                    firstIntervalById.put(processId, interval.getStartTime());
                }
                lastIntervalById.put(processId, interval.getEndTime());
                intervalBusyTime = Math.addExact(intervalBusyTime, duration);
                intervalBusyById.put(
                        processId,
                        Math.addExact(intervalBusyById.get(processId), duration));
            }
            expectedStart = interval.getEndTime();
        }
        if (expectedStart != completionTime || intervalBusyTime != busyTime) {
            throw new IllegalArgumentException(
                "Gantt intervals do not match the scheduled work and duration."
            );
        }
        for (Process process : resultOrder) {
            String processId = normalizePid(process.getPid());
            if (intervalBusyById.get(processId) != process.getBurstTime()
                    || !Integer.valueOf(process.getFirstStartTime()).equals(
                            firstIntervalById.get(processId))
                    || !Integer.valueOf(process.getCompletionTime()).equals(
                            lastIntervalById.get(processId))) {
                throw new IllegalArgumentException(
                    "Gantt intervals do not match each process schedule."
                );
            }
        }

        long elapsedTime = (long) completionTime - simulationStart;
        double utilization = resultOrder.isEmpty()
                ? 0.0
                : calculateCPUUtilization(busyTime, elapsedTime);
        int count = resultOrder.size();
        double averageTurnaround = count == 0
                ? 0.0 : (double) totalTurnaround / count;
        double averageWaiting = count == 0
                ? 0.0 : (double) totalWaiting / count;
        double averageResponse = count == 0
                ? 0.0 : (double) totalResponse / count;

        return new SchedulingResult(
                algorithmName,
                resultOrder,
                intervals,
                averageTurnaround,
                averageWaiting,
                averageResponse,
                busyTime,
                elapsedTime,
                utilization
        );
    }

    public static double calculateCPUUtilization(
            long busyTime, long totalElapsedTime) {
        if (busyTime < 0) {
            throw new IllegalArgumentException(
                "Busy time cannot be negative."
            );
        }
        if (totalElapsedTime <= 0) {
            throw new IllegalArgumentException(
                "Elapsed time must be positive."
            );
        }
        if (busyTime > totalElapsedTime) {
            throw new IllegalArgumentException(
                "Busy time cannot exceed elapsed time."
            );
        }
        return busyTime * 100.0 / totalElapsedTime;
    }

    public static void displayResult(SchedulingResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Scheduling result is null.");
        }

        System.out.println("\nScheduling Algorithm: "
                + result.getAlgorithmName());
        System.out.print("Execution Order: ");
        for (Process process : result.getExecutionOrder()) {
            System.out.print(process.getPid() + " ");
        }
        System.out.println();
        System.out.println("Gantt Chart (start-end: process):");
        if (result.getIntervals().isEmpty()) {
            System.out.println("(no execution)");
        } else {
            StringBuilder chart = new StringBuilder();
            for (Interval interval : result.getIntervals()) {
                if (chart.length() > 0) {
                    chart.append(" | ");
                }
                chart.append(interval);
            }
            System.out.println(chart);
        }

        System.out.println("\nProcess Performance Metrics");
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-8s %-8s %-8s %-8s %-8s %-8s%n",
                "PID", "CT", "TAT", "WT", "RT", "Burst");
        System.out.println("---------------------------------------------------------------");
        for (Process process : result.getExecutionOrder()) {
            System.out.printf("%-8s %-8d %-8d %-8d %-8d %-8d%n",
                    process.getPid(),
                    process.getCompletionTime(),
                    process.getTurnaroundTime(),
                    process.getWaitingTime(),
                    process.getResponseTime(),
                    process.getBurstTime());
        }
        System.out.println("---------------------------------------------------------------");
        System.out.printf("Average Turnaround Time : %.2f%n",
                result.getAverageTurnaroundTime());
        System.out.printf("Average Waiting Time    : %.2f%n",
                result.getAverageWaitingTime());
        System.out.printf("Average Response Time   : %.2f%n",
                result.getAverageResponseTime());
        System.out.printf("CPU Utilization         : %.2f%%%n",
                result.getCpuUtilization());
    }
}
