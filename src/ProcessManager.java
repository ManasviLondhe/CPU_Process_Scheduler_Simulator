
import java.util.ArrayList;
import java.util.List;

public class ProcessManager {

    private final List<Process> processes;
    private final HashTable processIndex;

    // Constructor
    public ProcessManager() {
        processes = new ArrayList<>();
        processIndex = new HashTable();
    }

    // Add a new process
    public boolean addProcess(Process p) {
        if (p == null) {
            return false;
        }

        if (processIndex.search(p.getPid()) != null) {
            return false;
        }

        if (!processIndex.insert(p)) {
            return false;
        }

        processes.add(p);
        return true;
    }

    // Delete a process using its PID
    public boolean deleteProcess(String pid) {

        if (pid == null || pid.trim().isEmpty()) {
            return false;
        }

        Process p = processIndex.delete(pid);

        if (p == null) {
            return false;
        }

        processes.remove(p);
        return true;
    }

    // Search for a process using its PID
    public Process searchProcess(String pid) {

        if (pid == null || pid.trim().isEmpty()) {
            return null;
        }

        return processIndex.search(pid);
    }

    // Display all processes
    public void displayProcesses() {

        if (processes.isEmpty()) {
            System.out.println("No processes available.");
            return;
        }

        System.out.println("\nProcess Details");
        System.out.println("------------------------------------------------");
        System.out.printf("%-15s %-10s %-10s %-10s%n",
                "PID", "Arrival", "Burst", "Priority");
        System.out.println("------------------------------------------------");

        for (Process p : processes) {
            System.out.printf("%-15s %-10d %-10d %-10d%n",
                    p.getPid(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority());
        }

        System.out.println("------------------------------------------------");
    }

    // The list is copied; Process instances remain shared.
    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    // Check whether the list is empty
    public boolean isEmpty() {
        return processes.isEmpty();
    }

    // Return the total number of processes
    public int getProcessCount() {
        return processes.size();
    }

    // Expose hash-table statistics for testing
    public int getHashTableCapacity() {
        return processIndex.capacity();
    }

    public double getHashTableLoadFactor() {
        return processIndex.loadFactor();
    }

    // Clear both data structures
    public void clearProcesses() {
        processes.clear();
        processIndex.clear();
    }
}