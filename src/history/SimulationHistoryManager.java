package history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.SimulationResult;
import util.FormatUtils;

/** Keeps the simulations completed during this application session (newest last). */
public class SimulationHistoryManager {
    private final List<HistoryEntry> entries = new ArrayList<>();

    public void add(SimulationResult result) {
        entries.add(new HistoryEntry(result));
    }

    public List<HistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }

    public void exportCsv(File file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write("Date/Time,Algorithm,Processes,Avg WT,Avg TAT,Avg RT,CPU Utilization %\n");
            for (HistoryEntry e : entries) {
                w.write(FormatUtils.dateTime(e.getTime()) + "," + FormatUtils.csv(e.getAlgorithm()) + "," + e.getProcessCount() + ","
                        + FormatUtils.decimal(e.getMetrics().getAvgWaitingTime()) + ","
                        + FormatUtils.decimal(e.getMetrics().getAvgTurnaroundTime()) + ","
                        + FormatUtils.decimal(e.getMetrics().getAvgResponseTime()) + ","
                        + FormatUtils.decimal(e.getMetrics().getCpuUtilization()) + "\n");
            }
        }
    }
}
