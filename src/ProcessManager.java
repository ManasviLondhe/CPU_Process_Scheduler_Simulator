
import java.util.ArrayList;
import java.util.List;

public class ProcessManager {

    private List<Process> processes;

    // Constructor
    public ProcessManager() {
        processes = new ArrayList<>();
    }

    // Add a new process
    public boolean addProcess(Process p) {

        // Check for duplicate PID
        if (searchProcess(p.pid) != null) {
            System.out.println("Process ID already exists!");
            return false;
        }

        // Validate process details
        if (p.arrivalTime < 0 || p.burstTime <= 0 || p.priority < 0) {
            System.out.println("Invalid process details!");
            return false;
        }

        processes.add(p);
        System.out.println("Process added successfully!");
        return true;
    }

    // Delete a process using its PID
    public boolean deleteProcess(String pid) {

        Process p = searchProcess(pid);

        if (p == null) {
            System.out.println("Process not found!");
            return false;
        }

        processes.remove(p);
        System.out.println("Process deleted successfully!");
        return true;
    }

    // Search for a process using its PID
    public Process searchProcess(String pid) {

        for (Process p : processes) {
            if (p.pid.equalsIgnoreCase(pid)) {
                return p;
            }
        }

        return null;
    }

    // Display all processes
    public void displayProcesses() {

        if (processes.isEmpty()) {
            System.out.println("No processes available.");
            return;
        }

        System.out.println("\nProcess Details");
        System.out.println("------------------------------------------------");
        System.out.printf("%-10s %-10s %-10s %-10s%n",
                "PID", "Arrival", "Burst", "Priority");
        System.out.println("------------------------------------------------");

        for (Process p : processes) {
            System.out.printf("%-10s %-10d %-10d %-10d%n",
                    p.pid, p.arrivalTime, p.burstTime, p.priority);
        }

        System.out.println("------------------------------------------------");
    }

    // Return the process list for scheduling algorithms
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
}