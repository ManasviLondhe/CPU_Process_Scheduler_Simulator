
import java.util.List;
import java.util.Scanner;

public class Scheduler {

    private List<Process> processes;

    // Constructor
    public Scheduler(List<Process> processes) {
        this.processes = processes;
    }

    // Display scheduling algorithm menu
    public void showSchedulingMenu() {

        if (processes.isEmpty()) {
            System.out.println("No processes available. Add processes first.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n===== CPU SCHEDULING ALGORITHMS =====");
            System.out.println("1. First Come First Serve (FCFS)");
            System.out.println("2. Shortest Job First (SJF)");
            System.out.println("3. Priority Scheduling");
            System.out.println("4. Round Robin");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.println("\nRunning FCFS Scheduling...");
                    FCFS.schedule(processes);
                    break;

                case 2:
                    System.out.println("\nSJF Scheduling is not implemented yet.");
                    // Later: SJF.schedule(processes);
                    break;

                case 3:
                    System.out.println("\nPriority Scheduling is not implemented yet.");
                    // Later: PriorityScheduler.schedule(processes);
                    break;

                case 4:
                    System.out.print("Enter time quantum: ");
                    int quantum = sc.nextInt();

                    if (quantum <= 0) {
                        System.out.println("Time quantum must be positive.");
                    } else {
                        System.out.println(
                                "\nRound Robin Scheduling is not implemented yet.");
                        // Later: RoundRobin.schedule(processes, quantum);
                    }
                    break;

                case 5:
                    System.out.println("Returning to main menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 5);
    }
}