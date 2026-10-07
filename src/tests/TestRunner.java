package tests;

/** Run this class (Run As > Java Application) to execute all tests. Exit code 1 on failure. */
public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) {
        DataStructureTests.run();
        SchedulerTests.run();
        MetricsAndValidationTests.run();
        System.out.println();
        System.out.println("Passed: " + TestUtil.getPassed() + "   Failed: " + TestUtil.getFailed());
        if (TestUtil.getFailed() > 0) {
            System.exit(1);
        }
        System.out.println("ALL TESTS PASSED");
    }
}
