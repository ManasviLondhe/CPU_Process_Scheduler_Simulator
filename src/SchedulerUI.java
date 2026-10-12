import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class SchedulerUI extends JFrame {

    private static final Color WHITE = Color.WHITE;
    private static final Color PAGE = new Color(245, 247, 250);
    private static final Color BORDER = new Color(217, 222, 229);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color ACCENT = new Color(14, 124, 134);
    private static final Color ACCENT_DARK = new Color(10, 92, 100);
    private static final Color ACCENT_LIGHT = new Color(224, 242, 243);
    private static final Color CHART_BG = new Color(239, 242, 245);
    private static final Color[] PROCESS_COLORS = {
        new Color(99, 102, 241),
        new Color(16, 185, 129),
        new Color(236, 72, 113),
        new Color(245, 158, 11),
        new Color(139, 92, 246),
        new Color(14, 165, 233),
        new Color(244, 114, 52),
        new Color(20, 184, 166),
        new Color(59, 130, 246),
        new Color(168, 85, 247)
    };
    private static final String[] PAGE_IDS = {
        "overview", "processes", "simulation", "comparison"
    };
    private static final String[] PAGE_TITLES = {
        "Dashboard", "Processes", "Simulation", "Compare algorithms"
    };
    private static final String[] PAGE_SUBTITLES = {
        "Your CPU scheduling workspace at a glance",
        "Build and manage the process workload",
        "Watch the selected scheduling algorithm run",
        "Compare every algorithm on the same workload"
    };
    private static final DecimalFormat DECIMAL = new DecimalFormat("0.00");

    private final ProcessManager manager = new ProcessManager();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final NavButton[] navButtons = new NavButton[PAGE_IDS.length];
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();
    private final JLabel dashboardProcessValue = new JLabel("0");
    private final JLabel dashboardAlgorithmValue = new JLabel("4");
    private final JLabel dashboardLastRunValue = new JLabel("Not run");
    private final JLabel dashboardWaitingValue = new JLabel("—");
    private final JLabel processPageCount = new JLabel("0 processes");
    private final JLabel simulationStatus = new JLabel("Ready to simulate");

    private final JTextField pidField = new JTextField();
    private final JTextField arrivalField = new JTextField();
    private final JTextField burstField = new JTextField();
    private final JTextField priorityField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JTextField quantumField = new JTextField("2");

    private final DefaultTableModel processModel = readOnlyModel(
            "PID", "Arrival", "Burst", "Priority");
    private final DefaultTableModel scheduleModel = readOnlyModel(
            "PID", "Arrival", "Burst", "Priority",
            "Start", "Completion", "Turnaround", "Waiting", "Response");
    private final DefaultTableModel comparisonModel = readOnlyModel(
            "Algorithm", "Avg waiting", "Avg turnaround",
            "Avg response", "CPU utilization");
    private final JTable processTable = new JTable(processModel);
    private final JTable scheduleTable = new JTable(scheduleModel);
    private final JTable comparisonTable = new JTable(comparisonModel);
    private final GanttPanel ganttPanel = new GanttPanel();
    private final JLabel[] resultMetrics = new JLabel[4];

    private List<Metrics.SchedulingResult> comparisonResults =
            new ArrayList<Metrics.SchedulingResult>();
    private Metrics.SchedulingResult lastResult;
    private String lastAlgorithm = "Not run";

    private SchedulerUI() {
        super("CPU Scheduler Lab");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1320, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE);
        setLayout(new BorderLayout());
        add(createNavigation(), BorderLayout.WEST);
        add(createWorkspace(), BorderLayout.CENTER);
        refreshProcessLists();
        showPage(0);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createNavigation() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(249, 250, 252));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        sidebar.setPreferredSize(new Dimension(206, 0));

        JLabel logo = new JLabel("  CPU Scheduler Lab");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(ACCENT_DARK);
        logo.setBorder(new EmptyBorder(23, 15, 5, 8));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(logo);
        JLabel tagline = new JLabel("  Process scheduling simulator");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tagline.setForeground(MUTED);
        tagline.setBorder(new EmptyBorder(0, 15, 23, 8));
        tagline.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(tagline);

        String[] labels = {"Dashboard", "Processes", "Simulation", "Compare"};
        for (int i = 0; i < labels.length; i++) {
            final int page = i;
            NavButton button = new NavButton(labels[i]);
            navButtons[i] = button;
            button.addActionListener(e -> showPage(page));
            sidebar.add(button);
        }
        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("CPU PROCESS SCHEDULER");
        footer.setFont(new Font("Segoe UI", Font.BOLD, 9));
        footer.setForeground(MUTED);
        footer.setBorder(new EmptyBorder(10, 18, 17, 10));
        footer.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(footer);
        return sidebar;
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(PAGE);
        workspace.add(createPageHeader(), BorderLayout.NORTH);

        pageHost.setBackground(PAGE);
        pageHost.add(createOverviewPage(), PAGE_IDS[0]);
        pageHost.add(createProcessesPage(), PAGE_IDS[1]);
        pageHost.add(createSimulationPage(), PAGE_IDS[2]);
        pageHost.add(createComparisonPage(), PAGE_IDS[3]);
        workspace.add(pageHost, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createPageHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(15, 23, 14, 23)));
        JPanel labels = new JPanel(new BorderLayout(0, 3));
        labels.setOpaque(false);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        pageTitle.setForeground(TEXT);
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pageSubtitle.setForeground(MUTED);
        labels.add(pageTitle, BorderLayout.NORTH);
        labels.add(pageSubtitle, BorderLayout.SOUTH);
        header.add(labels, BorderLayout.WEST);

        JLabel brand = new JLabel("SCHEDULER LAB");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brand.setForeground(ACCENT_DARK);
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 225, 226)),
                new EmptyBorder(7, 10, 7, 10)));
        header.add(brand, BorderLayout.EAST);
        return header;
    }

    private JPanel createOverviewPage() {
        JPanel page = pagePanel();
        JPanel metrics = new JPanel(new GridLayout(1, 4, 10, 0));
        metrics.setOpaque(false);
        metrics.add(summaryCard("Processes in workload", dashboardProcessValue,
                "Current queue"));
        metrics.add(summaryCard("Available algorithms", dashboardAlgorithmValue,
                "FCFS · SJF · Priority · RR"));
        metrics.add(summaryCard("Last simulation", dashboardLastRunValue,
                "Most recent run"));
        metrics.add(summaryCard("Average waiting", dashboardWaitingValue,
                "From last simulation"));
        page.add(metrics, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);

        JPanel quick = card(new BorderLayout(0, 12));
        quick.add(sectionHeading("Quick start",
                "The main tools for your scheduler"), BorderLayout.NORTH);
        JPanel quickContent = new JPanel();
        quickContent.setOpaque(false);
        quickContent.setLayout(new BoxLayout(quickContent, BoxLayout.Y_AXIS));
        quickContent.add(quickAction("1", "Create a workload",
                "Add processes with arrival, burst, and priority values.",
                "processes"));
        quickContent.add(Box.createVerticalStrut(12));
        quickContent.add(quickAction("2", "Run a scheduler",
                "Choose FCFS, SJF, Priority, or Round Robin.",
                "simulation"));
        quickContent.add(Box.createVerticalStrut(12));
        quickContent.add(quickAction("3", "Compare algorithms",
                "See the same workload scheduled four different ways.",
                "comparison"));
        quick.add(quickContent, BorderLayout.CENTER);
        center.add(quick);

        JPanel info = card(new BorderLayout(0, 12));
        info.add(sectionHeading("Scheduling algorithms",
                "The four schedulers included in this project"), BorderLayout.NORTH);
        JPanel descriptions = new JPanel(new GridLayout(4, 1, 0, 9));
        descriptions.setOpaque(false);
        descriptions.add(algorithmDescription("FCFS",
                "Runs processes in arrival order."));
        descriptions.add(algorithmDescription("SJF",
                "Runs the available process with the shortest burst."));
        descriptions.add(algorithmDescription("Priority",
                "Lower priority numbers run first."));
        descriptions.add(algorithmDescription("Round Robin",
                "Shares CPU time using the selected time quantum."));
        info.add(descriptions, BorderLayout.CENTER);
        center.add(info);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel createProcessesPage() {
        JPanel page = pagePanel();
        JPanel entry = card(new BorderLayout(0, 10));
        entry.add(sectionHeading("Add a process",
                "Process IDs must be unique. Burst time must be greater than zero."),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 4, 10, 0));
        fields.setOpaque(false);
        fields.add(fieldCell("Process ID", pidField));
        fields.add(fieldCell("Arrival time", arrivalField));
        fields.add(fieldCell("Burst time", burstField));
        fields.add(fieldCell("Priority  ·  lower is higher", priorityField));
        entry.add(fields, BorderLayout.CENTER);
        JButton add = button("Add process", ACCENT, WHITE);
        add.addActionListener(this::addProcess);
        entry.add(add, BorderLayout.EAST);
        page.add(entry, BorderLayout.NORTH);

        JPanel list = card(new BorderLayout(0, 10));
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);
        listHeader.add(sectionHeading("Workload",
                "Select a row to delete it"), BorderLayout.WEST);
        processPageCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        processPageCount.setForeground(ACCENT_DARK);
        listHeader.add(processPageCount, BorderLayout.EAST);
        list.add(listHeader, BorderLayout.NORTH);

        styleTable(processTable);
        JScrollPane processScroll = scroll(processTable);
        list.add(processScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(7, 0));
        search.setOpaque(false);
        searchField.setToolTipText("Search by process ID");
        styleField(searchField);
        searchField.addActionListener(e -> findProcess());
        JButton find = button("Find PID", new Color(239, 243, 248), TEXT);
        find.addActionListener(e -> findProcess());
        search.add(searchField, BorderLayout.CENTER);
        search.add(find, BorderLayout.EAST);
        search.setPreferredSize(new Dimension(300, 34));
        actions.add(search, BorderLayout.WEST);
        JButton delete = button("Delete selected", new Color(255, 241, 240),
                new Color(178, 63, 72));
        delete.addActionListener(e -> deleteSelectedProcess());
        actions.add(delete, BorderLayout.EAST);
        list.add(actions, BorderLayout.SOUTH);
        page.add(list, BorderLayout.CENTER);
        return page;
    }

    private JPanel createSimulationPage() {
        JPanel page = pagePanel();

        JPanel algorithms = card(new BorderLayout(0, 10));
        algorithms.add(sectionHeading("Choose an algorithm",
                "Run an algorithm to animate its timeline and calculate metrics"),
                BorderLayout.NORTH);
        JPanel choices = new JPanel(new GridLayout(1, 5, 9, 0));
        choices.setOpaque(false);
        choices.add(algorithmChoice("FCFS", "First Come, First Served", 1));
        choices.add(algorithmChoice("SJF", "Shortest Job First", 2));
        choices.add(algorithmChoice("Priority", "Lower number runs first", 3));

        JPanel rr = new JPanel(new BorderLayout(5, 5));
        rr.setOpaque(false);
        JButton rrButton = button("Round Robin", new Color(239, 243, 248), TEXT);
        rrButton.addActionListener(e -> runAlgorithm(4));
        styleField(quantumField);
        quantumField.setHorizontalAlignment(SwingConstants.CENTER);
        quantumField.setToolTipText("Positive Round Robin time quantum");
        quantumField.setPreferredSize(new Dimension(56, 34));
        rr.add(rrButton, BorderLayout.CENTER);
        JPanel qPanel = new JPanel(new BorderLayout(5, 0));
        qPanel.setOpaque(false);
        JLabel qLabel = muted("Time quantum");
        qLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        qPanel.add(qLabel, BorderLayout.WEST);
        qPanel.add(quantumField, BorderLayout.CENTER);
        rr.add(qPanel, BorderLayout.SOUTH);
        choices.add(rr);

        JButton compare = button("Compare all four", ACCENT_DARK, WHITE);
        compare.addActionListener(e -> compareAlgorithms());
        choices.add(compare);
        algorithms.add(choices, BorderLayout.CENTER);
        page.add(algorithms, BorderLayout.NORTH);

        JPanel chartCard = card(new BorderLayout(0, 8));
        JPanel chartTitle = new JPanel(new BorderLayout());
        chartTitle.setOpaque(false);
        chartTitle.add(sectionHeading("Gantt chart",
                "Colored blocks show which process is using the CPU over time"),
                BorderLayout.WEST);
        simulationStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        simulationStatus.setForeground(ACCENT_DARK);
        chartTitle.add(simulationStatus, BorderLayout.EAST);
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(scroll(ganttPanel), BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 8, 0));
        metrics.setOpaque(false);
        String[] captions = {
            "Average waiting time", "Average turnaround time",
            "Average response time", "CPU utilization"
        };
        for (int i = 0; i < resultMetrics.length; i++) {
            resultMetrics[i] = new JLabel("—");
            metrics.add(metricCard(captions[i], resultMetrics[i]));
        }
        chartCard.add(metrics, BorderLayout.SOUTH);

        JPanel processResults = card(new BorderLayout(0, 7));
        processResults.add(sectionHeading("Per-process results",
                "Start, completion, turnaround, waiting, and response time"),
                BorderLayout.NORTH);
        styleTable(scheduleTable);
        processResults.add(scroll(scheduleTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, chartCard, processResults);
        split.setResizeWeight(0.48);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setDividerSize(8);
        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private JPanel createComparisonPage() {
        JPanel page = pagePanel();
        JPanel intro = card(new BorderLayout(8, 0));
        intro.add(sectionHeading("Compare all four algorithms",
                "Each algorithm schedules the same process workload"),
                BorderLayout.CENTER);
        JButton run = button("Run comparison", ACCENT, WHITE);
        run.addActionListener(e -> compareAlgorithms());
        intro.add(run, BorderLayout.EAST);
        page.add(intro, BorderLayout.NORTH);

        JPanel results = card(new BorderLayout(0, 10));
        results.add(sectionHeading("Comparison results",
                "Select a row and open its timeline to see the full Gantt chart"),
                BorderLayout.NORTH);
        styleTable(comparisonTable);
        comparisonTable.setRowHeight(38);
        results.add(scroll(comparisonTable), BorderLayout.CENTER);
        JButton open = button("View selected timeline", ACCENT_DARK, WHITE);
        open.addActionListener(e -> openSelectedComparison());
        comparisonTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedComparison();
                }
            }
        });
        results.add(open, BorderLayout.SOUTH);
        page.add(results, BorderLayout.CENTER);
        return page;
    }

    private JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PAGE);
        panel.setBorder(new EmptyBorder(17, 20, 18, 20));
        return panel;
    }

    private JPanel summaryCard(String caption, JLabel value, String detail) {
        JPanel card = card(new BorderLayout(0, 7));
        JLabel captionLabel = muted(caption);
        captionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        value.setFont(new Font("Segoe UI", Font.BOLD, 23));
        value.setForeground(ACCENT_DARK);
        JLabel detailLabel = muted(detail);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        card.add(captionLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(detailLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel metricCard(String caption, JLabel value) {
        JPanel panel = card(new BorderLayout(0, 3));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)));
        JLabel label = muted(caption);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        value.setFont(new Font("Segoe UI", Font.BOLD, 15));
        value.setForeground(ACCENT_DARK);
        panel.add(label, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JPanel quickAction(
            String number, String title, String detail, final String pageId) {
        JPanel panel = new JPanel(new BorderLayout(11, 0));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(9, 10, 9, 10));
        JLabel badge = new JLabel(number, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(ACCENT_LIGHT);
        badge.setForeground(ACCENT_DARK);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setPreferredSize(new Dimension(31, 31));
        panel.add(badge, BorderLayout.WEST);
        JPanel labels = new JPanel(new BorderLayout(0, 3));
        labels.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT);
        JLabel detailLabel = muted(detail);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labels.add(titleLabel, BorderLayout.NORTH);
        labels.add(detailLabel, BorderLayout.CENTER);
        panel.add(labels, BorderLayout.CENTER);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < PAGE_IDS.length; i++) {
                    if (PAGE_IDS[i].equals(pageId)) {
                        showPage(i);
                        break;
                    }
                }
            }
        });
        return panel;
    }

    private JPanel algorithmDescription(String title, String description) {
        JPanel row = new JPanel(new BorderLayout(9, 0));
        row.setOpaque(false);
        JLabel bullet = new JLabel("●");
        bullet.setForeground(ACCENT);
        row.add(bullet, BorderLayout.WEST);
        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.BOLD, 11));
        name.setForeground(TEXT);
        JLabel detail = muted(description);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        text.add(name, BorderLayout.NORTH);
        text.add(detail, BorderLayout.CENTER);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JPanel algorithmChoice(String name, String detail, int algorithm) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 9, 8, 9)));
        JLabel title = new JLabel(name);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(TEXT);
        panel.add(title, BorderLayout.NORTH);
        JButton run = button("Run  →", ACCENT_LIGHT, ACCENT_DARK);
        run.addActionListener(e -> runAlgorithm(algorithm));
        panel.add(run, BorderLayout.CENTER);
        JLabel caption = muted(detail);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        panel.add(caption, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel fieldCell(String name, JTextField field) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        JLabel label = muted(name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        styleField(field);
        cell.add(label, BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        return cell;
    }

    private JPanel sectionHeading(String title, String subtitle) {
        JPanel heading = new JPanel(new BorderLayout(0, 3));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = muted(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        heading.add(titleLabel, BorderLayout.NORTH);
        heading.add(subtitleLabel, BorderLayout.SOUTH);
        return heading;
    }

    private void showPage(int page) {
        pageLayout.show(pageHost, PAGE_IDS[page]);
        pageTitle.setText(PAGE_TITLES[page]);
        pageSubtitle.setText(PAGE_SUBTITLES[page]);
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].setActive(i == page);
        }
    }

    private void addProcess(ActionEvent event) {
        try {
            String pid = pidField.getText().trim();
            Process process = new Process(
                    pid,
                    parseInteger(arrivalField, "Arrival time"),
                    parseInteger(burstField, "Burst time"),
                    parseInteger(priorityField, "Priority"));
            if (!manager.addProcess(process)) {
                showError("That process ID already exists.");
                return;
            }
            pidField.setText("");
            arrivalField.setText("");
            burstField.setText("");
            priorityField.setText("");
            refreshProcessLists();
            invalidateResults();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void findProcess() {
        String pid = searchField.getText().trim();
        if (pid.isEmpty()) {
            showError("Enter a process ID to search.");
            return;
        }
        Process found = manager.searchProcess(pid);
        if (found == null) {
            showError("No process found with ID " + pid + ".");
            return;
        }
        for (int row = 0; row < processModel.getRowCount(); row++) {
            if (String.valueOf(processModel.getValueAt(row, 0))
                    .equalsIgnoreCase(found.getPid())) {
                processTable.setRowSelectionInterval(row, row);
                processTable.scrollRectToVisible(
                        processTable.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private void deleteSelectedProcess() {
        int selected = processTable.getSelectedRow();
        if (selected < 0) {
            showError("Select a process from the workload first.");
            return;
        }
        String pid = String.valueOf(processModel.getValueAt(selected, 0));
        if (!manager.deleteProcess(pid)) {
            showError("The selected process could not be found.");
            return;
        }
        refreshProcessLists();
        invalidateResults();
    }

    private void runAlgorithm(int algorithm) {
        if (manager.isEmpty()) {
            showError("Add at least one process before running a scheduler.");
            return;
        }
        try {
            Integer quantum = algorithm == 4 ? readQuantum() : null;
            Metrics.SchedulingResult result = Scheduler.schedule(
                    algorithm, manager.getProcesses(), quantum);
            showResult(result);
            lastAlgorithm = result.getAlgorithmName();
            updateOverview();
            showPage(2);
        } catch (IllegalArgumentException | ArithmeticException ex) {
            showError(ex.getMessage());
        }
    }

    private void compareAlgorithms() {
        if (manager.isEmpty()) {
            showError("Add at least one process before comparing algorithms.");
            return;
        }
        try {
            comparisonResults = Scheduler.scheduleAll(
                    manager.getProcesses(), readQuantum());
            comparisonModel.setRowCount(0);
            for (Metrics.SchedulingResult result : comparisonResults) {
                comparisonModel.addRow(new Object[] {
                    result.getAlgorithmName(),
                    decimal(result.getAverageWaitingTime()),
                    decimal(result.getAverageTurnaroundTime()),
                    decimal(result.getAverageResponseTime()),
                    decimal(result.getCpuUtilization()) + "%"
                });
            }
            showPage(3);
        } catch (IllegalArgumentException | ArithmeticException ex) {
            showError(ex.getMessage());
        }
    }

    private void openSelectedComparison() {
        int selected = comparisonTable.getSelectedRow();
        if (selected < 0 || selected >= comparisonResults.size()) {
            showError("Select an algorithm from the comparison results.");
            return;
        }
        Metrics.SchedulingResult result = comparisonResults.get(selected);
        showResult(result);
        lastAlgorithm = result.getAlgorithmName();
        updateOverview();
        showPage(2);
    }

    private void showResult(Metrics.SchedulingResult result) {
        lastResult = result;
        ganttPanel.play(result);
        List<Process> processes = result.getExecutionOrder();
        scheduleModel.setRowCount(0);
        for (Process process : processes) {
            scheduleModel.addRow(new Object[] {
                process.getPid(),
                process.getArrivalTime(),
                process.getBurstTime(),
                process.getPriority(),
                process.getFirstStartTime(),
                process.getCompletionTime(),
                process.getTurnaroundTime(),
                process.getWaitingTime(),
                process.getResponseTime()
            });
        }
        resultMetrics[0].setText(decimal(result.getAverageWaitingTime()));
        resultMetrics[1].setText(decimal(result.getAverageTurnaroundTime()));
        resultMetrics[2].setText(decimal(result.getAverageResponseTime()));
        resultMetrics[3].setText(decimal(result.getCpuUtilization()) + "%");
        simulationStatus.setText("Animating " + result.getAlgorithmName());
        simulationStatus.setForeground(ACCENT_DARK);
    }

    private void invalidateResults() {
        lastResult = null;
        lastAlgorithm = "Not run";
        scheduleModel.setRowCount(0);
        comparisonModel.setRowCount(0);
        comparisonResults.clear();
        ganttPanel.clear();
        for (JLabel metric : resultMetrics) {
            metric.setText("—");
        }
        simulationStatus.setText("Workload changed · run again");
        dashboardWaitingValue.setText("—");
        dashboardLastRunValue.setText("Not run");
        dashboardProcessValue.setText(String.valueOf(manager.getProcessCount()));
    }

    private int parseInteger(JTextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private Integer readQuantum() {
        int quantum = parseInteger(quantumField, "Time quantum");
        if (quantum <= 0) {
            throw new IllegalArgumentException(
                    "Time quantum must be greater than zero.");
        }
        return quantum;
    }

    private void refreshProcessLists() {
        processModel.setRowCount(0);
        for (Process process : manager.getProcesses()) {
            processModel.addRow(new Object[] {
                process.getPid(), process.getArrivalTime(),
                process.getBurstTime(), process.getPriority()
            });
        }
        String count = manager.getProcessCount()
                + (manager.getProcessCount() == 1 ? " process" : " processes");
        processPageCount.setText(count);
        dashboardProcessValue.setText(String.valueOf(manager.getProcessCount()));
    }

    private void updateOverview() {
        dashboardLastRunValue.setText(lastAlgorithm);
        dashboardWaitingValue.setText(
                lastResult == null ? "—" : decimal(lastResult.getAverageWaitingTime()));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Please check your input",
                JOptionPane.WARNING_MESSAGE);
    }

    private static String decimal(double value) {
        return DECIMAL.format(value);
    }

    private static JPanel card(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        return panel;
    }

    private static JPanel card() {
        return card(new BorderLayout());
    }

    private static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return label;
    }

    private static JScrollPane scroll(java.awt.Component component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(WHITE);
        return pane;
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 8, 6, 8)));
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(237, 240, 244));
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.equals(ACCENT)
                        || background.equals(ACCENT_DARK) ? background : BORDER),
                new EmptyBorder(7, 10, 7, 10)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static Color colorForPid(String pid) {
        return PROCESS_COLORS[Math.floorMod(pid.hashCode(), PROCESS_COLORS.length)];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Using default Swing look and feel: "
                        + ex.getMessage());
            }
            new SchedulerUI().setVisible(true);
        });
    }

    private final class NavButton extends JButton {
        private boolean selected;

        NavButton(String text) {
            super("   " + text);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, 16, 11, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
            setAlignmentX(LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean value) {
            selected = value;
            setFont(new Font("Segoe UI", value ? Font.BOLD : Font.PLAIN, 12));
            setForeground(value ? ACCENT_DARK : TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            if (selected) {
                g.setColor(ACCENT_LIGHT);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(ACCENT);
                g.fillRect(0, 0, 4, getHeight());
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class GanttPanel extends JPanel {
        private static final int LEFT = 18;
        private static final int TOP = 38;
        private static final int BAR_HEIGHT = 58;
        private static final int HEIGHT = 170;
        private static final long ANIMATION_DURATION = 3600;

        private Metrics.SchedulingResult result;
        private long animationStart;
        private javax.swing.Timer timer;

        GanttPanel() {
            setBackground(CHART_BG);
            setPreferredSize(new Dimension(680, HEIGHT));
            setMinimumSize(new Dimension(420, HEIGHT));
        }

        void play(Metrics.SchedulingResult next) {
            stopTimer();
            result = next;
            long end = Math.max(1L, next.getElapsedTime());
            int unit = pixelsPerUnit(end);
            long width = LEFT * 2L + end * unit + 28;
            setPreferredSize(new Dimension((int) Math.min(10000L,
                    Math.max(680L, width)), HEIGHT));
            animationStart = System.currentTimeMillis();
            timer = new javax.swing.Timer(30, e -> {
                repaint();
                int scale = pixelsPerUnit(Math.max(1L, result.getElapsedTime()));
                int cursorX = LEFT + (int) Math.round(currentTime() * scale);
                scrollRectToVisible(new java.awt.Rectangle(
                        cursorX, 0, 1, getHeight()));
                if (System.currentTimeMillis() - animationStart
                        >= ANIMATION_DURATION) {
                    ((javax.swing.Timer) e.getSource()).stop();
                    timer = null;
                    repaint();
                }
            });
            timer.start();
            revalidate();
            repaint();
        }

        void clear() {
            stopTimer();
            result = null;
            setPreferredSize(new Dimension(680, HEIGHT));
            revalidate();
            repaint();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        private static int pixelsPerUnit(long total) {
            if (total <= 20) {
                return 36;
            }
            if (total <= 50) {
                return 23;
            }
            if (total <= 100) {
                return 14;
            }
            if (total <= 250) {
                return 8;
            }
            if (total <= 600) {
                return 4;
            }
            return 2;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(CHART_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.setColor(TEXT);
            g.drawString("Gantt Chart", LEFT, 22);

            if (result == null) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.setColor(MUTED);
                centered(g, "Choose an algorithm to see its schedule",
                        getWidth() / 2, 91);
                g.dispose();
                return;
            }

            long total = Math.max(1L, result.getElapsedTime());
            int scale = pixelsPerUnit(total);
            double progress = Math.min(1.0,
                    (System.currentTimeMillis() - animationStart)
                            / (double) ANIMATION_DURATION);
            double currentTime = total * progress;
            int axisY = TOP + BAR_HEIGHT + 8;
            int legendY = HEIGHT - 17;
            int lastLabelEnd = -100;
            Map<String, Color> colors = new LinkedHashMap<String, Color>();
            List<Metrics.Interval> intervals = result.getIntervals();

            for (Metrics.Interval interval : intervals) {
                if (interval.isIdle()) {
                    continue;
                }
                colors.put(interval.getProcessId(),
                        colorForPid(interval.getProcessId()));
            }

            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String totalText = "TOTAL: " + result.getElapsedTime() + " units";
            int textWidth = g.getFontMetrics().stringWidth(totalText);
            g.setColor(new Color(224, 229, 234));
            g.fillRoundRect(getWidth() - textWidth - 36, 10,
                    textWidth + 22, 21, 10, 10);
            g.setColor(TEXT);
            g.drawString(totalText, getWidth() - textWidth - 25, 24);

            g.setColor(new Color(220, 225, 231));
            g.fillRoundRect(LEFT, TOP, (int) (total * scale),
                    BAR_HEIGHT, 10, 10);

            for (Metrics.Interval interval : intervals) {
                double start = interval.getStartTime();
                double end = interval.getEndTime();
                if (start >= currentTime) {
                    break;
                }
                double visibleEnd = Math.min(currentTime, end);
                int x = LEFT + (int) Math.round(start * scale);
                int right = LEFT + (int) Math.round(visibleEnd * scale);
                int width = Math.max(0, right - x - 2);
                if (width <= 0) {
                    continue;
                }
                String pid = interval.isIdle()
                        ? "Idle" : interval.getProcessId();
                Color fill = interval.isIdle()
                        ? new Color(194, 201, 209) : colorForPid(pid);
                g.setColor(fill);
                g.fillRoundRect(x, TOP + 2, width,
                        BAR_HEIGHT - 4, 8, 8);

                if (width >= 29) {
                    g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g.setColor(Color.WHITE);
                    centered(g, pid, x + width / 2, TOP + 27);
                }
                if (!interval.isIdle() && width >= 43) {
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g.setColor(new Color(255, 255, 255, 220));
                    centered(g, (int) (visibleEnd - start) + "u",
                            x + width / 2, TOP + 43);
                }

                String label = String.valueOf(interval.getStartTime());
                g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                int labelX = x - g.getFontMetrics().stringWidth(label) / 2;
                if (labelX > lastLabelEnd + 4) {
                    g.setColor(MUTED);
                    g.drawString(label, labelX, axisY + 13);
                    lastLabelEnd = labelX + g.getFontMetrics().stringWidth(label);
                }
            }

            if (progress >= 1.0) {
                int finalX = LEFT + (int) (total * scale);
                String finalLabel = String.valueOf(result.getElapsedTime());
                g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g.setColor(MUTED);
                g.drawString(finalLabel,
                        finalX - g.getFontMetrics().stringWidth(finalLabel) / 2,
                        axisY + 13);
            }

            int legendX = LEFT;
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (Map.Entry<String, Color> entry : colors.entrySet()) {
                int labelWidth = g.getFontMetrics().stringWidth(entry.getKey());
                if (legendX + labelWidth + 25 > getWidth() - 8) {
                    break;
                }
                g.setColor(entry.getValue());
                g.fillOval(legendX, legendY - 7, 9, 9);
                g.setColor(TEXT);
                g.drawString(entry.getKey(), legendX + 13, legendY + 1);
                legendX += labelWidth + 29;
            }

            int cursorX = LEFT + (int) Math.round(currentTime * scale);
            g.setColor(ACCENT_DARK);
            g.fillRoundRect(cursorX - 1, TOP - 6, 2, BAR_HEIGHT + 15, 2, 2);
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g.setColor(ACCENT_DARK);
            g.drawString("NOW  " + Math.min(result.getElapsedTime(),
                    (int) currentTime), Math.max(LEFT, cursorX - 16), TOP - 12);
            g.dispose();
        }

        private double currentTime() {
            if (result == null) {
                return 0;
            }
            double progress = Math.min(1.0,
                    (System.currentTimeMillis() - animationStart)
                            / (double) ANIMATION_DURATION);
            return Math.max(0, result.getElapsedTime()) * progress;
        }

        private static void centered(
                Graphics2D g, String text, int centerX, int baseline) {
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
        }
    }
}
