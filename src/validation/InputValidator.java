package validation;

import java.util.Collection;

import model.Process;
import model.QueueLevelConfig;
import model.QueuePolicy;
import model.SimulationConfig;
import model.SchedulingAlgorithmType;
import util.Constants;

/** All input validation. Methods throw IllegalArgumentException with a user-friendly message. */
public final class InputValidator {
    private InputValidator() {
    }

    public static int parseInt(String text, String label) {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    /**
     * @param ignorePid PID of the process being edited (it may keep its own PID), or null when adding
     */
    public static void validateProcess(String pid, int arrival, int burst, int priority, int queueLevel,
                                       String name, Collection<Process> existing, String ignorePid) {
        if (pid == null || pid.trim().isEmpty()) {
            throw new IllegalArgumentException("PID cannot be empty.");
        }
        String id = pid.trim();
        if (id.length() > Constants.MAX_PID_LENGTH || !id.matches("[A-Za-z0-9_\\-]+")) {
            throw new IllegalArgumentException("PID may contain only letters, digits, '_' and '-' (max "
                    + Constants.MAX_PID_LENGTH + " characters).");
        }
        for (Process p : existing) {
            if (p.getPid().equalsIgnoreCase(id) && (ignorePid == null || !p.getPid().equalsIgnoreCase(ignorePid))) {
                throw new IllegalArgumentException("PID already exists.");
            }
        }
        if (name != null && name.trim().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Process name is too long (max " + Constants.MAX_NAME_LENGTH + ").");
        }
        if (arrival < 0) {
            throw new IllegalArgumentException("Arrival time must be 0 or greater.");
        }
        if (arrival > Constants.MAX_ARRIVAL_TIME) {
            throw new IllegalArgumentException("Arrival time must not exceed " + Constants.MAX_ARRIVAL_TIME + ".");
        }
        if (burst <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than 0.");
        }
        if (burst > Constants.MAX_BURST_TIME) {
            throw new IllegalArgumentException("Burst time must not exceed " + Constants.MAX_BURST_TIME + ".");
        }
        if (priority < 0 || priority > Constants.MAX_PRIORITY) {
            throw new IllegalArgumentException("Priority must be between 0 and " + Constants.MAX_PRIORITY + ".");
        }
        if (queueLevel < 1 || queueLevel > Constants.MAX_QUEUE_LEVELS) {
            throw new IllegalArgumentException("Queue must be between 1 and " + Constants.MAX_QUEUE_LEVELS + ".");
        }
        if (ignorePid == null && existing.size() >= Constants.MAX_PROCESSES) {
            throw new IllegalArgumentException("At most " + Constants.MAX_PROCESSES + " processes are supported.");
        }
    }

    public static void validateWorkload(Collection<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Please add at least one process.");
        }
    }

    public static void validateConfig(SimulationConfig c) {
        if (c.getAlgorithm() == SchedulingAlgorithmType.ROUND_ROBIN && c.getTimeQuantum() <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than 0.");
        }
        if (c.isAgingEnabled() && c.getAgingInterval() <= 0) {
            throw new IllegalArgumentException("Aging interval must be greater than 0.");
        }
        if (c.getContextSwitchCost() < 0) {
            throw new IllegalArgumentException("Context switch cost must be 0 or greater.");
        }
        if (c.getStarvationThreshold() < 0) {
            throw new IllegalArgumentException("Starvation threshold must be 0 (off) or greater.");
        }
        if (c.getAlgorithm() == SchedulingAlgorithmType.MLQ) {
            validateLevels(c.getMlqLevels());
        } else if (c.getAlgorithm() == SchedulingAlgorithmType.MLFQ) {
            validateLevels(c.getMlfqLevels());
        }
    }

    private static void validateLevels(java.util.List<QueueLevelConfig> levels) {
        if (levels.isEmpty() || levels.size() > Constants.MAX_QUEUE_LEVELS) {
            throw new IllegalArgumentException("Number of queues must be between 1 and " + Constants.MAX_QUEUE_LEVELS + ".");
        }
        for (QueueLevelConfig l : levels) {
            if (l.getPolicy() == QueuePolicy.ROUND_ROBIN && l.getQuantum() <= 0) {
                throw new IllegalArgumentException("Time quantum must be greater than 0 (" + l.getName() + ").");
            }
        }
    }
}
