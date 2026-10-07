package metrics;

import java.util.List;

import model.MetricsSummary;
import model.Process;
import model.ProcessState;
import model.ScheduleSegment;
import model.SegmentType;

/**
 * All performance formulas live here (no maths in UI code).
 * <pre>
 * TAT = completion - arrival          WT = TAT - burst          RT = first start - arrival
 * CPU utilization = busy process time / total time * 100   (context switch time counts as overhead)
 * Throughput      = completed processes / total time
 * </pre>
 */
public final class MetricsCalculator {
    private MetricsCalculator() {
    }

    public static void applyProcessMetrics(Process p) {
        p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
        p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
        p.setResponseTime(p.getFirstStartTime() - p.getArrivalTime());
    }

    public static MetricsSummary calculate(List<Process> processes, List<ScheduleSegment> segments,
                                           int totalTime, int contextSwitches, int contextSwitchTime) {
        int done = 0;
        long wt = 0;
        long tat = 0;
        long rt = 0;
        int preemptions = 0;
        for (Process p : processes) {
            preemptions += p.getPreemptions();
            if (p.getState() == ProcessState.COMPLETED) {
                done++;
                wt += p.getWaitingTime();
                tat += p.getTurnaroundTime();
                rt += p.getResponseTime();
            }
        }
        int busy = 0;
        int idle = 0;
        for (ScheduleSegment s : segments) {
            if (s.getType() == SegmentType.PROCESS) {
                busy += s.getLength();
            } else if (s.getType() == SegmentType.IDLE) {
                idle += s.getLength();
            }
        }
        double avgWt = done == 0 ? 0 : (double) wt / done;
        double avgTat = done == 0 ? 0 : (double) tat / done;
        double avgRt = done == 0 ? 0 : (double) rt / done;
        double util = totalTime == 0 ? 0 : 100.0 * busy / totalTime;
        double throughput = totalTime == 0 ? 0 : (double) done / totalTime;
        return new MetricsSummary(done, avgWt, avgTat, avgRt, util, throughput, totalTime, busy, idle,
                contextSwitches, contextSwitchTime, preemptions);
    }
}
