
public class ProcessManagerTest {

    public static void main(String[] args) {

        ProcessManager manager = new ProcessManager();

        // Add processes
        manager.addProcess(new Process("P101", 0, 5, 2));
        manager.addProcess(new Process("P111", 2, 3, 1));
        manager.addProcess(new Process("P121", 1, 7, 3));

        // Case-insensitive duplicate must be rejected
        manager.addProcess(new Process("p101", 4, 2, 1));

        // Search
        System.out.println("\nSearch P111:");
        System.out.println(manager.searchProcess("P111"));

        System.out.println("\nSearch using lowercase PID:");
        System.out.println(manager.searchProcess("p111"));

        // Display
        manager.displayProcesses();

        // Independent copies for scheduling
        Process original = manager.searchProcess("P101");
        Process copy = original.copy();
        copy.setRemainingTime(1);

        System.out.println("\nOriginal remaining time: "
                + original.getRemainingTime());
        System.out.println("Copy remaining time: "
                + copy.getRemainingTime());

        // Sort
        manager.sortByArrivalTime();
        System.out.println("\nAfter sorting:");
        manager.displayProcesses();

        // Delete
        manager.deleteProcess("p101");
        System.out.println("\nSearch after deletion: "
                + manager.searchProcess("P101"));

        // Trigger rehashing
        for (int i = 0; i < 20; i++) {
            manager.addProcess(
                "TASK" + i, i, 2, 1
            );
        }

        System.out.println("\nProcess count: "
                + manager.getProcessCount());
        System.out.println("Hash capacity: "
                + manager.getHashTableCapacity());
        System.out.println("Load factor: "
                + manager.getHashTableLoadFactor());

        // Clear
        manager.clearProcesses();
        System.out.println("Manager empty: " + manager.isEmpty());
    }
}