
public class Process {

    // Basic process information
    private final String pid;
    private int arrivalTime;
    private int burstTime;
    private int priority;

    // Runtime scheduling information
    private int remainingTime;
    private int completionTime;
    private int firstStartTime;

    // Scheduling metrics
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        if (pid == null || pid.trim().isEmpty()) {
            throw new IllegalArgumentException("PID cannot be empty.");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be positive.");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("Priority cannot be negative.");
        }

        this.pid = pid.trim();
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;

        resetSchedulingData();
    }

    public String getPid() {
        return pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getFirstStartTime() {
        return firstStartTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setArrivalTime(int arrivalTime) {
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Invalid arrival time.");
        }

        this.arrivalTime = arrivalTime;
        resetSchedulingData();
    }

    public void setBurstTime(int burstTime) {
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Invalid burst time.");
        }

        this.burstTime = burstTime;
        resetSchedulingData();
    }

    public void setPriority(int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Invalid priority.");
        }

        this.priority = priority;
        resetSchedulingData();
    }

    public void setRemainingTime(int remainingTime) {
        if (remainingTime < 0 || remainingTime > burstTime) {
            throw new IllegalArgumentException("Invalid remaining time.");
        }

        this.remainingTime = remainingTime;
    }

    public void setCompletionTime(int completionTime) {
        if (completionTime < arrivalTime
                || firstStartTime < 0
                || completionTime < firstStartTime) {
            throw new IllegalArgumentException("Invalid completion time.");
        }

        this.completionTime = completionTime;
    }

    public void setFirstStartTime(int firstStartTime) {
        if (firstStartTime < arrivalTime) {
            throw new IllegalArgumentException("Invalid first start time.");
        }

        this.firstStartTime = firstStartTime;
    }

    public void setWaitingTime(int waitingTime) {
        if (waitingTime < 0) {
            throw new IllegalArgumentException("Invalid waiting time.");
        }

        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        if (turnaroundTime < 0) {
            throw new IllegalArgumentException("Invalid turnaround time.");
        }

        this.turnaroundTime = turnaroundTime;
    }

    public void setResponseTime(int responseTime) {
        if (responseTime < 0) {
            throw new IllegalArgumentException("Invalid response time.");
        }

        this.responseTime = responseTime;
    }

    public final void resetSchedulingData() {
        remainingTime = burstTime;
        completionTime = 0;
        firstStartTime = -1;
        waitingTime = 0;
        turnaroundTime = 0;
        responseTime = 0;
    }

    public Process copy() {
        return new Process(pid, arrivalTime, burstTime, priority);
    }

    Process snapshot() {
        Process snapshot = new Process(pid, arrivalTime, burstTime, priority);
        snapshot.remainingTime = remainingTime;
        snapshot.completionTime = completionTime;
        snapshot.firstStartTime = firstStartTime;
        snapshot.waitingTime = waitingTime;
        snapshot.turnaroundTime = turnaroundTime;
        snapshot.responseTime = responseTime;
        return snapshot;
    }

    @Override
    public String toString() {
        return String.format(
            "PID: %s | Arrival: %d | Burst: %d | Priority: %d "
                + "| Remaining: %d | Completion: %d "
                + "| Waiting: %d | Turnaround: %d | Response: %d",
            pid,
            arrivalTime,
            burstTime,
            priority,
            remainingTime,
            completionTime,
            waitingTime,
            turnaroundTime,
            responseTime
        );
    }
}