package export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import model.MetricsSummary;
import model.Process;
import model.ScheduleSegment;
import model.SimulationEvent;
import model.SimulationResult;
import util.FormatUtils;

/** Writes simulation results to CSV files (opens in Excel / any text editor). */
public final class SimulationExporter {
    private SimulationExporter() {
    }

    public static void exportResult(SimulationResult r, File file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write(toCsv(r));
        }
    }

    public static String toCsv(SimulationResult r) {
        StringBuilder sb = new StringBuilder();
        MetricsSummary m = r.getMetrics();
        sb.append("CPU Process Scheduler Simulator - result export\n");
        sb.append("Algorithm,").append(FormatUtils.csv(r.getLabel())).append('\n');
        sb.append("Configuration,").append(FormatUtils.csv(r.getConfig().describe())).append('\n');
        sb.append("Finished at,").append(FormatUtils.dateTime(r.getFinishedAt())).append("\n\n");

        sb.append("PROCESS RESULTS\n");
        sb.append("PID,Name,Arrival,Burst,Priority,Start,Completion,Turnaround,Waiting,Response,Preemptions,ContextSwitches\n");
        for (Process p : r.getProcesses()) {
            sb.append(FormatUtils.csv(p.getPid())).append(',').append(FormatUtils.csv(p.getName())).append(',')
              .append(p.getArrivalTime()).append(',').append(p.getBurstTime()).append(',').append(p.getPriority()).append(',')
              .append(p.getFirstStartTime()).append(',').append(p.getCompletionTime()).append(',')
              .append(p.getTurnaroundTime()).append(',').append(p.getWaitingTime()).append(',')
              .append(p.getResponseTime()).append(',').append(p.getPreemptions()).append(',')
              .append(p.getContextSwitches()).append('\n');
        }
        sb.append("\nMETRICS\n");
        sb.append("Average waiting time,").append(FormatUtils.decimal(m.getAvgWaitingTime())).append('\n');
        sb.append("Average turnaround time,").append(FormatUtils.decimal(m.getAvgTurnaroundTime())).append('\n');
        sb.append("Average response time,").append(FormatUtils.decimal(m.getAvgResponseTime())).append('\n');
        sb.append("CPU utilization,").append(FormatUtils.percent(m.getCpuUtilization())).append('\n');
        sb.append("Throughput (processes/unit),").append(FormatUtils.decimal(m.getThroughput())).append('\n');
        sb.append("Total execution time,").append(m.getTotalTime()).append('\n');
        sb.append("Total idle time,").append(m.getIdleTime()).append('\n');
        sb.append("Context switches,").append(m.getContextSwitches()).append('\n');
        sb.append("Context switch time,").append(m.getContextSwitchTime()).append('\n');
        sb.append("Preemptions,").append(m.getPreemptions()).append('\n');

        sb.append("\nGANTT CHART\nBlock,Start,End\n");
        for (ScheduleSegment s : r.getSegments()) {
            sb.append(s.getLabel()).append(',').append(s.getStart()).append(',').append(s.getEnd()).append('\n');
        }
        sb.append("\nEVENT LOG\nTime,Type,Message\n");
        for (SimulationEvent e : r.getEvents()) {
            sb.append(e.getTime()).append(',').append(e.getType()).append(',').append(FormatUtils.csv(e.getMessage())).append('\n');
        }
        return sb.toString();
    }

    public static void exportComparison(List<SimulationResult> results, File file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write("Algorithm,Avg WT,Avg TAT,Avg RT,CPU Utilization %,Throughput,Total Time,Context Switches,Preemptions\n");
            for (SimulationResult r : results) {
                MetricsSummary m = r.getMetrics();
                w.write(FormatUtils.csv(r.getLabel()) + "," + FormatUtils.decimal(m.getAvgWaitingTime()) + ","
                        + FormatUtils.decimal(m.getAvgTurnaroundTime()) + "," + FormatUtils.decimal(m.getAvgResponseTime()) + ","
                        + FormatUtils.decimal(m.getCpuUtilization()) + "," + FormatUtils.decimal(m.getThroughput()) + ","
                        + m.getTotalTime() + "," + m.getContextSwitches() + "," + m.getPreemptions() + "\n");
            }
        }
    }
}
