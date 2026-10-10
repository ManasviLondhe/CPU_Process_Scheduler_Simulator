import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ProcessManager manager = new ProcessManager();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                displayMainMenu();
                Integer choice = readInt(scanner, "Enter your choice: ");
                if (choice == null) {
                    break;
                }

                try {
                    switch (choice) {
                        case 1:
                            addProcess(scanner, manager);
                            break;
                        case 2:
                            deleteProcess(scanner, manager);
                            break;
                        case 3:
                            searchProcess(scanner, manager);
                            break;
                        case 4:
                            manager.displayProcesses();
                            break;
                        case 5:
                            runOneAlgorithm(scanner, manager, 1);
                            break;
                        case 6:
                            runOneAlgorithm(scanner, manager, 2);
                            break;
                        case 7:
                            runOneAlgorithm(scanner, manager, 3);
                            break;
                        case 8:
                            runOneAlgorithm(scanner, manager, 4);
                            break;
                        case 9:
                            compareAlgorithms(scanner, manager);
                            break;
                        case 10:
                            running = false;
                            System.out.println("Exiting simulator.");
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (IllegalArgumentException | ArithmeticException ex) {
                    System.out.println("Unable to complete operation: "
                            + ex.getMessage());
                }
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n==================================");
        System.out.println("  CPU PROCESS SCHEDULER SIMULATOR");
        System.out.println("==================================");
        System.out.println("1. Add Process");
        System.out.println("2. Delete Process");
        System.out.println("3. Search Process");
        System.out.println("4. Display All Processes");
        System.out.println("5. Run FCFS");
        System.out.println("6. Run SJF");
        System.out.println("7. Run Priority Scheduling");
        System.out.println("8. Run Round Robin");
        System.out.println("9. Compare All Algorithms");
        System.out.println("10. Exit");
    }

    private static void addProcess(Scanner scanner, ProcessManager manager) {
        String pid = readToken(scanner, "Enter Process ID: ");
        if (pid == null) {
            return;
        }
        if (manager.searchProcess(pid) != null) {
            System.out.println("Process ID already exists.");
            return;
        }

        Integer arrival = readInt(scanner, "Enter Arrival Time: ");
        if (arrival == null) {
            return;
        }
        Integer burst = readInt(scanner, "Enter Burst Time: ");
        if (burst == null) {
            return;
        }
        Integer priority = readInt(
                scanner, "Enter Priority (smaller = higher): ");
        if (priority == null) {
            return;
        }

        Process process = new Process(pid, arrival, burst, priority);
        if (manager.addProcess(process)) {
            System.out.println("Process added successfully.");
        } else {
            System.out.println("Process could not be added.");
        }
    }

    private static void deleteProcess(
            Scanner scanner, ProcessManager manager) {
        String pid = readToken(scanner, "Enter PID to delete: ");
        if (pid == null) {
            return;
        }
        System.out.println(manager.deleteProcess(pid)
                ? "Process deleted."
                : "Process not found.");
    }

    private static void searchProcess(
            Scanner scanner, ProcessManager manager) {
        String pid = readToken(scanner, "Enter PID to search: ");
        if (pid == null) {
            return;
        }
        Process process = manager.searchProcess(pid);
        if (process == null) {
            System.out.println("Process not found.");
            return;
        }
        displayProcess(process);
    }

    private static void runOneAlgorithm(
            Scanner scanner, ProcessManager manager, int algorithm) {
        if (manager.isEmpty()) {
            System.out.println("No processes available. Add processes first.");
            return;
        }

        Integer quantum = null;
        if (algorithm == 4) {
            quantum = readPositiveInt(scanner, "Enter time quantum: ");
            if (quantum == null) {
                return;
            }
        }
        Metrics.displayResult(
                Scheduler.schedule(algorithm, manager.getProcesses(), quantum));
    }

    private static void compareAlgorithms(
            Scanner scanner, ProcessManager manager) {
        if (manager.isEmpty()) {
            System.out.println("No processes available. Add processes first.");
            return;
        }
        Integer quantum = readPositiveInt(
                scanner, "Enter time quantum for Round Robin: ");
        if (quantum == null) {
            return;
        }
        for (Metrics.SchedulingResult result
                : Scheduler.scheduleAll(manager.getProcesses(), quantum)) {
            Metrics.displayResult(result);
        }
    }

    private static Integer readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            Integer value = readInt(scanner, prompt);
            if (value == null || value > 0) {
                return value;
            }
            System.out.println("Value must be greater than zero.");
        }
    }

    private static Integer readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNext()) {
                return null;
            }
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            System.out.println("Enter a valid integer.");
            scanner.next();
        }
    }

    private static String readToken(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.hasNext() ? scanner.next() : null;
    }

    private static void displayProcess(Process process) {
        System.out.println("\nProcess Details");
        System.out.println("PID: " + process.getPid());
        System.out.println("Arrival Time: " + process.getArrivalTime());
        System.out.println("Burst Time: " + process.getBurstTime());
        System.out.println("Priority: " + process.getPriority());
    }
}
