
import java.util.ArrayList;
import java.util.Comparator;
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

        // Reject null input
        if (p == null) {
            System.out.println("Invalid process!");
            return false;
        }

        // Validate process details
        if (p.getPid() == null || p.getPid().trim().isEmpty()
                || p.getArrivalTime() < 0
                || p.getBurstTime() <= 0
                || p.getPriority() < 0) {
            System.out.println("Invalid process details!");
            return false;
        }

        // Check for duplicate PID using the hash table
        if (processIndex.search(p.getPid()) != null) {
            System.out.println("Process ID already exists!");
            return false;
        }

        // Add to the hash table first
        if (!processIndex.insert(p)) {
            System.out.println("Process could not be added!");
            return false;
        }

        // Keep the list and hash table synchronized
        processes.add(p);

        System.out.println("Process added successfully!");
        return true;
    }

    // Delete a process using its PID
    public boolean deleteProcess(String pid) {

        if (pid == null || pid.trim().isEmpty()) {
            System.out.println("Invalid process ID!");
            return false;
        }

        // Delete from hash table
        Process p = processIndex.delete(pid);

        if (p == null) {
            System.out.println("Process not found!");
            return false;
        }

        // Remove the same object from the list
        processes.remove(p);

        System.out.println("Process deleted successfully!");
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

    // Return a shallow copy of the process list.
    // The Process objects themselves are shared.
    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    // Return independent copies for a scheduling run
    public List<Process> getProcessCopies() {
        List<Process> copies = new ArrayList<>();

        for (Process p : processes) {
            copies.add(p.copy());
        }

        return copies;
    }

    // Sort by arrival time, then PID
    public void sortByArrivalTime() {
        processes.sort(
            Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(
                    Process::getPid,
                    String.CASE_INSENSITIVE_ORDER
                )
        );
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

        System.out.println("All processes cleared.");
    }
}