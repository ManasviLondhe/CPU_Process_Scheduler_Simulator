package export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import model.Process;
import util.FormatUtils;
import validation.InputValidator;

/** Saves / loads a workload as CSV: pid,name,arrival,burst,priority,queue. */
public final class WorkloadFileService {
    private static final String HEADER = "pid,name,arrival,burst,priority,queue";

    private WorkloadFileService() {
    }

    public static void save(List<Process> processes, File file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            w.write(HEADER + "\n");
            for (Process p : processes) {
                w.write(FormatUtils.csv(p.getPid()) + "," + FormatUtils.csv(p.getName()) + "," + p.getArrivalTime() + ","
                        + p.getBurstTime() + "," + p.getPriority() + "," + p.getQueueLevel() + "\n");
            }
        }
    }

    /** @throws IllegalArgumentException with the line number when a row is invalid */
    public static List<Process> load(File file) throws IOException {
        List<Process> result = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty() || (lineNo == 1 && line.toLowerCase().startsWith("pid"))) {
                    continue;
                }
                String[] f = line.split(",", -1);
                try {
                    if (f.length < 6) {
                        throw new IllegalArgumentException("Expected 6 columns: " + HEADER);
                    }
                    int at = InputValidator.parseInt(f[2], "Arrival time");
                    int bt = InputValidator.parseInt(f[3], "Burst time");
                    int pr = InputValidator.parseInt(f[4], "Priority");
                    int q = InputValidator.parseInt(f[5], "Queue");
                    InputValidator.validateProcess(f[0], at, bt, pr, q, f[1], result, null);
                    result.add(new Process(f[0].trim(), f[1], at, bt, pr, q));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Line " + lineNo + ": " + e.getMessage());
                }
            }
        }
        return result;
    }
}
