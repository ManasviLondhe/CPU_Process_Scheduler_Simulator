package generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Process;

/** Creates random workloads. Passing a seed makes the result reproducible. */
public class ProcessGenerator {

    public List<Process> generate(int count, int minArrival, int maxArrival, int minBurst, int maxBurst,
                                  int minPriority, int maxPriority, int queueLevels, Long seed, int startIndex) {
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("Number of processes must be between 1 and 100.");
        }
        if (minArrival < 0 || maxArrival < minArrival) {
            throw new IllegalArgumentException("Arrival range is invalid (min must be 0 or greater and not above max).");
        }
        if (minBurst < 1 || maxBurst < minBurst) {
            throw new IllegalArgumentException("Burst range is invalid (min must be 1 or greater and not above max).");
        }
        if (minPriority < 0 || maxPriority < minPriority) {
            throw new IllegalArgumentException("Priority range is invalid.");
        }
        Random rnd = seed == null ? new Random() : new Random(seed);
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String pid = "P" + (startIndex + i);
            list.add(new Process(pid, "Process " + (startIndex + i),
                    between(rnd, minArrival, maxArrival), between(rnd, minBurst, maxBurst),
                    between(rnd, minPriority, maxPriority), between(rnd, 1, Math.max(1, queueLevels))));
        }
        return list;
    }

    public List<Process> generatePreset(WorkloadPreset preset, Long seed, int startIndex) {
        if (!preset.isMixed()) {
            return generate(preset.getCount(), 0, preset.getMaxArrival(), preset.getMinBurst(),
                    preset.getMaxBurst(), 1, preset.getMaxPriority(), 3, seed, startIndex);
        }
        Random rnd = seed == null ? new Random() : new Random(seed);
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < preset.getCount(); i++) {
            boolean shortJob = rnd.nextInt(100) < 60;
            int burst = shortJob ? between(rnd, 1, 4) : between(rnd, 15, preset.getMaxBurst());
            String pid = "P" + (startIndex + i);
            list.add(new Process(pid, "Process " + (startIndex + i), between(rnd, 0, preset.getMaxArrival()),
                    burst, between(rnd, 1, preset.getMaxPriority()), between(rnd, 1, 3)));
        }
        return list;
    }

    private static int between(Random rnd, int min, int max) {
        return min + rnd.nextInt(max - min + 1);
    }
}
