package model;

import java.time.LocalDateTime;
import java.util.List;

/** Immutable outcome of one finished simulation; everything the UI, comparison and export need. */
public class SimulationResult {
    private final String label;
    private final SimulationConfig config;
    private final List<Process> processes;
    private final List<ScheduleSegment> segments;
    private final List<SimulationEvent> events;
    private final MetricsSummary metrics;
    private final LocalDateTime finishedAt;

    public SimulationResult(String label, SimulationConfig config, List<Process> processes,
                            List<ScheduleSegment> segments, List<SimulationEvent> events,
                            MetricsSummary metrics) {
        this.label = label;
        this.config = config;
        this.processes = processes;
        this.segments = segments;
        this.events = events;
        this.metrics = metrics;
        this.finishedAt = LocalDateTime.now();
    }

    /** Same result under a different display label (used by what-if analysis). */
    public SimulationResult withLabel(String newLabel) {
        return new SimulationResult(newLabel, config, processes, segments, events, metrics);
    }

    public String getLabel() { return label; }
    public SimulationConfig getConfig() { return config; }
    public List<Process> getProcesses() { return processes; }
    public List<ScheduleSegment> getSegments() { return segments; }
    public List<SimulationEvent> getEvents() { return events; }
    public MetricsSummary getMetrics() { return metrics; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}
