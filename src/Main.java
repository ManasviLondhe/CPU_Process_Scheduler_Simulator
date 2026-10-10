
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();

        int choice;

        do {
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
            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("Enter a valid number: ");
                sc.next();
            }

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.print("Enter Process ID: ");
                    String pid = sc.next();

                    if (findProcess(processes, pid) != null) {
                        System.out.println("Process ID already exists.");
                        break;
                    }

                    System.out.print("Enter Arrival Time: ");
                    int arrival = sc.nextInt();

                    System.out.print("Enter Burst Time: ");
                    int burst = sc.nextInt();

                    System.out.print("Enter Priority (smaller = higher): ");
                    int priority = sc.nextInt();

                    if (arrival < 0 || burst <= 0 || priority < 0) {
                        System.out.println("Invalid process details.");
                        break;
                    }

                    processes.add(
                        new Process(pid, arrival, burst, priority)
                    );

                    System.out.println("Process added successfully.");
                    break;

                case 2:
                    System.out.print("Enter PID to delete: ");
                    String deleteId = sc.next();

                    Process toDelete = findProcess(processes, deleteId);

                    if (toDelete != null) {
                        processes.remove(toDelete);
                        System.out.println("Process deleted.");
                    } else {
                        System.out.println("Process not found.");
                    }
                    break;

                case 3:
                    System.out.print("Enter PID to search: ");
                    String searchId = sc.next();

                    Process found = findProcess(processes, searchId);

                    if (found != null) {
                        displayProcess(found);
                    } else {
                        System.out.println("Process not found.");
                    }
                    break;

                case 4:
                    if (processes.isEmpty()) {
                        System.out.println("No processes available.");
                    } else {
                        System.out.println(
                            "\nPID\tArrival\tBurst\tPriority"
                        );

                        for (Process p : processes) {
                            System.out.println(
                                p.pid + "\t" +
                                p.arrivalTime + "\t" +
                                p.burstTime + "\t" +
                                p.priority
                            );
                        }
                    }
                    break;

                case 5:
                    FCFS.schedule(processes);
                    break;

                case 6:
                    SJF.schedule(processes);
                    break;

                case 7:
                    PriorityScheduler.schedule(processes);
                    break;

                case 8:
                    if (processes.isEmpty()) {
                        System.out.println("No processes available.");
                        break;
                    }

                    int timeQuantum;
                    do {
                        System.out.print("Enter time quantum: ");
                        while (!sc.hasNextInt()) {
                            System.out.print(
                                "Enter a valid positive integer: "
                            );
                            sc.next();
                        }
                        timeQuantum = sc.nextInt();
                        if (timeQuantum <= 0) {
                            System.out.println(
                                "Time quantum must be greater than zero."
                            );
                        }
                    } while (timeQuantum <= 0);

                    new RoundRobin(timeQuantum).schedule(processes);
                    break;

                case 9:
    if (processes.isEmpty()) {
        System.out.println("No processes available.");
        break;
    }

    System.out.println("\n===== FCFS =====");
    FCFS.schedule(processes);

    System.out.println("\n===== SJF =====");
    SJF.schedule(processes);

    System.out.println("\n===== Priority Scheduling =====");
    PriorityScheduler.schedule(processes);

    System.out.println("\n===== Round Robin =====");
    System.out.print("Enter time quantum: ");

    int quantum = sc.nextInt();

    if (quantum > 0) {
        new RoundRobin(quantum).schedule(processes);
    } else {
        System.out.println("Time quantum must be positive.");
    }
    break;

                case 10:
                    System.out.println("Exiting simulator.");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 10);

        sc.close();
    }

    static Process findProcess(List<Process> processes, String pid) {
        for (Process p : processes) {
            if (p.pid.equalsIgnoreCase(pid)) {
                return p;
            }
        }
        return null;
    }

    static void displayProcess(Process p) {
        System.out.println("\nProcess Details");
        System.out.println("PID: " + p.pid);
        System.out.println("Arrival Time: " + p.arrivalTime);
        System.out.println("Burst Time: " + p.burstTime);
        System.out.println("Priority: " + p.priority);
    }

}