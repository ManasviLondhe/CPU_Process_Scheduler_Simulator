package scheduler;

import java.util.function.BiConsumer;

import model.Process;
import model.SimulationConfig;

/** Shared plumbing: name, config, listener and "no-op" defaults for non-preemptive algorithms. */
public abstract class AbstractScheduler implements Scheduler {
    protected final SimulationConfig config;
    private final String name;
    private BiConsumer<Process, String> queueChangeListener;

    protected AbstractScheduler(String name, SimulationConfig config) {
        this.name = name;
        this.config = config;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int quantumFor(Process process) {
        return NO_QUANTUM;
    }

    @Override
    public boolean shouldPreempt(Process running) {
        return false;
    }

    @Override
    public void onTimeUnit() {
        // nothing to do by default
    }

    @Override
    public void setQueueChangeListener(BiConsumer<Process, String> listener) {
        this.queueChangeListener = listener;
    }

    protected void notifyQueueChange(Process process, String message) {
        if (queueChangeListener != null) {
            queueChangeListener.accept(process, message);
        }
    }
}
