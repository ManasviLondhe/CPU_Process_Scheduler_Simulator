
public class Process {

    // 1. Basic process information
    private int pid;
    private int arrivalTime;
    private int burstTime;
    private int priority;

    // 2. Runtime scheduling information
    private int remainingTime;
    private int completionTime;
    private int firstStartTime;

    // 3. Calculated performance metrics
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;

    // Constructor
    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;

        // Initial runtime values
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.firstStartTime = -1;

        // Metrics are calculated after scheduling
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = 0;
    }

    // Getters: retrieve process information

    public int getPid() {
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

    // Setters: update process information

    public void setArrivalTime(int arrivalTime) {
        if (arrivalTime < 0) {
            throw new IllegalArgumentException(
                "Arrival time cannot be negative."
            );
        }
        this.arrivalTime = arrivalTime;
    }

    public void setBurstTime(int burstTime) {
        if (burstTime <= 0) {
            throw new IllegalArgumentException(
                "Burst time must be greater than zero."
            );
        }

        this.burstTime = burstTime;
        this.remainingTime = burstTime;

        // Reset calculated values because burst time changed
        resetSchedulingData();
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setRemainingTime(int remainingTime) {
        if (remainingTime < 0 || remainingTime > burstTime) {
            throw new IllegalArgumentException(
                "Remaining time must be between 0 and burst time."
            );
        }
        this.remainingTime = remainingTime;
    }

    public void setCompletionTime(int completionTime) {
        if (completionTime < 0) {
            throw new IllegalArgumentException(
                "Completion time cannot be negative."
            );
        }
        this.completionTime = completionTime;
        calculateTurnaroundTime();
        calculateWaitingTime();
    }

    public void setFirstStartTime(int firstStartTime) {
        if (firstStartTime < -1) {
            throw new IllegalArgumentException(
                "First start time must be -1 or non-negative."
            );
        }
        this.firstStartTime = firstStartTime;
        calculateResponseTime();
    }

    public void setWaitingTime(int waitingTime) {
        if (waitingTime < 0) {
            throw new IllegalArgumentException(
                "Waiting time cannot be negative."
            );
        }
        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        if (turnaroundTime < 0) {
            throw new IllegalArgumentException(
                "Turnaround time cannot be negative."
            );
        }
        this.turnaroundTime = turnaroundTime;
    }

    public void setResponseTime(int responseTime) {
        if (responseTime < 0) {
            throw new IllegalArgumentException(
                "Response time cannot be negative."
            );
        }
        this.responseTime = responseTime;
    }

    // Calculate turnaround time = completion time - arrival time
    public void calculateTurnaroundTime() {
        if (completionTime >= arrivalTime) {
            this.turnaroundTime = completionTime - arrivalTime;
        }
    }

    // Calculate waiting time = turnaround time - burst time
    public void calculateWaitingTime() {
        if (turnaroundTime >= burstTime) {
            this.waitingTime = turnaroundTime - burstTime;
        }
    }

    // Calculate response time = first start time - arrival time
    public void calculateResponseTime() {
        if (firstStartTime >= arrivalTime) {
            this.responseTime = firstStartTime - arrivalTime;
        }
    }

    // Reset execution results before a new scheduling run
    public void resetSchedulingData() {
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.firstStartTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = 0;
    }

    // Create an independent copy for scheduling simulations
    public Process copy() {
        return new Process(pid, arrivalTime, burstTime, priority);
    }

    // Display process information
    @Override
    public String toString() {
        return String.format(
            "PID: %d | Arrival: %d | Burst: %d | Priority: %d "
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