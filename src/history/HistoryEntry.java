package history;

import java.time.LocalDateTime;

import model.MetricsSummary;
import model.SimulationResult;

/** One row of the history page; keeps the full result so it can be re-opened. */
public class HistoryEntry {
    private final LocalDateTime time;
    private final String algorithm;
    private final int processCount;
    private final MetricsSummary metrics;
    private final SimulationResult result;

    public HistoryEntry(SimulationResult result) {
        this.time = result.getFinishedAt();
        this.algorithm = result.getConfig().describe();
        this.processCount = result.getProcesses().size();
        this.metrics = result.getMetrics();
        this.result = result;
    }

    public LocalDateTime getTime() { return time; }
    public String getAlgorithm() { return algorithm; }
    public int getProcessCount() { return processCount; }
    public MetricsSummary getMetrics() { return metrics; }
    public SimulationResult getResult() { return result; }
}
