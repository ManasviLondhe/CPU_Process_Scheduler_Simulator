package model;

/**
 * A process of the simulated system.
 * The identity fields (pid, name, arrival, burst, priority, queueLevel) never change;
 * the remaining fields are runtime state updated by the simulation engine.
 * <p>
 * queueLevel is the 1-based class a process belongs to (used by MLQ).
 * currentQueueLevel is the 1-based level the process currently sits in (MLQ / MLFQ).
 */
public class Process {
    private final String pid;
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private final int queueLevel;

    private int remainingTime;
    private ProcessState state = ProcessState.NEW;
    private int firstStartTime = -1;
    private int completionTime = -1;
    private int turnaroundTime;
    private int waitingTime;
    private int responseTime;
    private int totalCpuTime;
    private int contextSwitches;
    private int preemptions;
    private int currentQueueLevel = 1;
    private int effectivePriority;
    private int agingCounter;
    private int waitStreak;
    private boolean starvationFlagged;

    public Process(String pid, String name, int arrivalTime, int burstTime, int priority, int queueLevel) {
        this.pid = pid;
        this.name = (name == null || name.trim().isEmpty()) ? pid : name.trim();
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.queueLevel = queueLevel;
        this.remainingTime = burstTime;
        this.effectivePriority = priority;
        this.currentQueueLevel = queueLevel;
    }

    public Process(String pid, int arrivalTime, int burstTime) {
        this(pid, pid, arrivalTime, burstTime, 1, 1);
    }

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this(pid, pid, arrivalTime, burstTime, priority, 1);
    }

    /** A new process with the same identity and all runtime state reset. */
    public Process freshCopy() {
        return new Process(pid, name, arrivalTime, burstTime, priority, queueLevel);
    }

    /** An exact copy including runtime state (used for simulation results). */
    public Process snapshot() {
        Process p = freshCopy();
        p.remainingTime = remainingTime;
        p.state = state;
        p.firstStartTime = firstStartTime;
        p.completionTime = completionTime;
        p.turnaroundTime = turnaroundTime;
        p.waitingTime = waitingTime;
        p.responseTime = responseTime;
        p.totalCpuTime = totalCpuTime;
        p.contextSwitches = contextSwitches;
        p.preemptions = preemptions;
        p.currentQueueLevel = currentQueueLevel;
        p.effectivePriority = effectivePriority;
        return p;
    }

    /** Natural PID ordering: P2 comes before P10. Used as the last tie-breaker everywhere. */
    public static int comparePid(String a, String b) {
        int ia = digitSuffixStart(a);
        int ib = digitSuffixStart(b);
        int prefix = a.substring(0, ia).compareTo(b.substring(0, ib));
        if (prefix != 0) {
            return prefix;
        }
        if (ia < a.length() && ib < b.length() && a.length() - ia < 18 && b.length() - ib < 18) {
            int c = Long.compare(Long.parseLong(a.substring(ia)), Long.parseLong(b.substring(ib)));
            if (c != 0) {
                return c;
            }
        }
        return a.compareTo(b);
    }

    private static int digitSuffixStart(String s) {
        int i = s.length();
        while (i > 0 && Character.isDigit(s.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    // ----- runtime updates -----
    public void consumeOneUnit() {
        remainingTime--;
        totalCpuTime++;
    }

    public void incrementWaiting() { waitingTime++; waitStreak++; }
    public void incrementContextSwitches() { contextSwitches++; }
    public void incrementPreemptions() { preemptions++; }

    // ----- getters / setters -----
    public String getPid() { return pid; }
    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getQueueLevel() { return queueLevel; }
    public int getRemainingTime() { return remainingTime; }
    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }
    public int getFirstStartTime() { return firstStartTime; }
    public void setFirstStartTime(int t) { this.firstStartTime = t; }
    public int getCompletionTime() { return completionTime; }
    public void setCompletionTime(int t) { this.completionTime = t; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int t) { this.turnaroundTime = t; }
    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int t) { this.waitingTime = t; }
    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int t) { this.responseTime = t; }
    public int getTotalCpuTime() { return totalCpuTime; }
    public int getContextSwitches() { return contextSwitches; }
    public int getPreemptions() { return preemptions; }
    public int getCurrentQueueLevel() { return currentQueueLevel; }
    public void setCurrentQueueLevel(int level) { this.currentQueueLevel = level; }
    public int getEffectivePriority() { return effectivePriority; }
    public void setEffectivePriority(int p) { this.effectivePriority = p; }
    public int getAgingCounter() { return agingCounter; }
    public void setAgingCounter(int c) { this.agingCounter = c; }
    public int getWaitStreak() { return waitStreak; }
    public void setWaitStreak(int s) { this.waitStreak = s; }
    public boolean isStarvationFlagged() { return starvationFlagged; }
    public void setStarvationFlagged(boolean f) { this.starvationFlagged = f; }

    @Override
    public String toString() {
        return pid;
    }
}
