package controller;

/** Identifiers of the application pages (used by navigation). */
public enum PageId {
    DASHBOARD("Dashboard"), PROCESSES("Processes"), ALGORITHMS("Algorithms"), SIMULATION("Simulation"),
    RESULTS("Results"), COMPARE("Compare"), HISTORY("History"), ABOUT("About");

    private final String title;

    PageId(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
