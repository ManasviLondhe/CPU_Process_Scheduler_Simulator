package simulation;

import model.Process;

/** State of the simulated CPU: idle, running a process, or busy with a context switch. */
public class CPU {
    public enum Status { IDLE, RUNNING, CONTEXT_SWITCHING }

    private Status status = Status.IDLE;
    private Process process;
    private Process pending;
    private int sliceUsed;
    private int switchRemaining;

    public void run(Process p) {
        status = Status.RUNNING;
        process = p;
        pending = null;
        sliceUsed = 0;
    }

    public void beginSwitch(Process next, int cost) {
        status = Status.CONTEXT_SWITCHING;
        pending = next;
        process = null;
        switchRemaining = cost;
    }

    public void tickSwitch() {
        switchRemaining--;
    }

    /** Ends the switch and returns the process that was waiting for the CPU. */
    public Process finishSwitch() {
        Process next = pending;
        pending = null;
        status = Status.IDLE;
        return next;
    }

    public void release() {
        status = Status.IDLE;
        process = null;
        sliceUsed = 0;
    }

    public void addSlice() { sliceUsed++; }
    public boolean isIdle() { return status == Status.IDLE; }
    public boolean isRunning() { return status == Status.RUNNING; }
    public boolean isSwitching() { return status == Status.CONTEXT_SWITCHING; }
    public Status getStatus() { return status; }
    public Process getProcess() { return process; }
    public Process getPendingProcess() { return pending; }
    public int getSliceUsed() { return sliceUsed; }
    public int getSwitchRemaining() { return switchRemaining; }
}
