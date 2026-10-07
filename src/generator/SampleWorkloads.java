package generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import model.Process;

/** Predefined workloads with hand-verified expected results (see tests.SchedulerTests). */
public final class SampleWorkloads {
    private SampleWorkloads() {
    }

    /** Test 1: P1(0,5) P2(1,3) P3(2,2). */
    public static List<Process> basic() {
        return list(new Process("P1", 0, 5), new Process("P2", 1, 3), new Process("P3", 2, 2));
    }

    /** Test 2: CPU idle between t=3 and t=8. */
    public static List<Process> idle() {
        return list(new Process("P1", 0, 3), new Process("P2", 8, 4));
    }

    /** Test 3: Round Robin, quantum 2. */
    public static List<Process> roundRobin() {
        return list(new Process("P1", 0, 5), new Process("P2", 0, 4), new Process("P3", 0, 3));
    }

    /** Test 4: priority preemption (lower number = higher priority). */
    public static List<Process> priorityPreemption() {
        return list(new Process("P1", 0, 6, 3), new Process("P2", 2, 4, 1), new Process("P3", 3, 2, 2));
    }

    /** Test 5: SRTF preemption. */
    public static List<Process> srtfPreemption() {
        return list(new Process("P1", 0, 8), new Process("P2", 1, 4), new Process("P3", 2, 2), new Process("P4", 3, 1));
    }

    /** Test 6: a long job and a stream of short jobs (MLFQ starvation / aging demo). */
    public static List<Process> mlfqStarvation() {
        return list(new Process("P1", 0, 6), new Process("S1", 2, 2), new Process("S2", 4, 2),
                new Process("S3", 6, 2), new Process("S4", 8, 2));
    }

    /** Test 7: Multilevel Queue demo; the last argument of the 6-arg constructor is the queue class. */
    public static List<Process> mlqMixed() {
        return list(new Process("A", "Background job", 0, 3, 1, 3),
                new Process("B", "Interactive job", 1, 3, 1, 2),
                new Process("C", "System job", 2, 2, 1, 1));
    }

    public static Map<String, Supplier<List<Process>>> all() {
        Map<String, Supplier<List<Process>>> m = new LinkedHashMap<>();
        m.put("Test 1 - Basic (FCFS / SJF / SRTF)", SampleWorkloads::basic);
        m.put("Test 2 - CPU idle period", SampleWorkloads::idle);
        m.put("Test 3 - Round Robin (use quantum 2)", SampleWorkloads::roundRobin);
        m.put("Test 4 - Priority preemption", SampleWorkloads::priorityPreemption);
        m.put("Test 5 - SRTF preemption", SampleWorkloads::srtfPreemption);
        m.put("Test 6 - MLFQ starvation / aging", SampleWorkloads::mlfqStarvation);
        m.put("Test 7 - Multilevel Queue classes", SampleWorkloads::mlqMixed);
        return m;
    }

    private static List<Process> list(Process... processes) {
        List<Process> l = new ArrayList<>();
        for (Process p : processes) {
            l.add(p);
        }
        return l;
    }
}
