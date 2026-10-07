package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import comparison.AlgorithmComparisonService;
import controller.ApplicationController;
import export.SimulationExporter;
import model.MetricsSummary;
import model.SchedulingAlgorithmType;
import model.SimulationResult;
import ui.components.BarChartPanel;
import ui.components.ThemedButton;
import util.FormatUtils;
import util.UIUtils;
import validation.InputValidator;

/** Runs the SAME workload through several algorithms (or several Round Robin quanta) and compares them. */
public class ComparisonPanel extends BasePage {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"Algorithm", "Avg WT", "Avg TAT", "Avg RT", "CPU Util.", "Throughput",
        "Context Switches", "Preemptions"};

    private final transient AlgorithmComparisonService service = new AlgorithmComparisonService();
    private final Map<SchedulingAlgorithmType, JCheckBox> checks = new EnumMap<>(SchedulingAlgorithmType.class);
    private final JTextField quantumField = new JTextField("1,2,4,8", 12);
    private final JLabel note = UIUtils.muted(" ");
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final BarChartPanel[] charts = {new BarChartPanel(), new BarChartPanel(), new BarChartPanel(),
        new BarChartPanel(), new BarChartPanel()};
    private transient List<SimulationResult> results = new ArrayList<>();

    public ComparisonPanel(ApplicationController app) {
        super(app, "Compare Algorithms", "Identical workload, different schedulers - fair comparison");

        JPanel algoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        algoRow.setBackground(UIUtils.WHITE);
        algoRow.add(UIUtils.title("Algorithms:"));
        for (SchedulingAlgorithmType t : SchedulingAlgorithmType.values()) {
            JCheckBox cb = new JCheckBox(t.getDisplayName(), true);
            cb.setBackground(UIUtils.WHITE);
            checks.put(t, cb);
            algoRow.add(cb);
        }
        ThemedButton run = new ThemedButton("Run Comparison", ThemedButton.Kind.PRIMARY);
        run.addActionListener(e -> runComparison());
        algoRow.add(run);

        JPanel whatIf = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        whatIf.setBackground(UIUtils.WHITE);
        whatIf.add(UIUtils.title("What-if (Round Robin quantum values):"));
        whatIf.add(quantumField);
        ThemedButton wi = new ThemedButton("Run What-If");
        wi.addActionListener(e -> runWhatIf());
        whatIf.add(wi);
        ThemedButton export = new ThemedButton("Export CSV");
        export.addActionListener(e -> export());
        whatIf.add(export);

        JPanel controls = UIUtils.card(new BorderLayout(0, 4));
        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 2));
        rows.setBackground(UIUtils.WHITE);
        rows.add(algoRow);
        rows.add(whatIf);
        controls.add(rows, BorderLayout.CENTER);
        controls.add(note, BorderLayout.SOUTH);
        body.add(controls, BorderLayout.NORTH);

        JTable table = new JTable(model);
        UIUtils.styleTable(table);
        JPanel chartGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        chartGrid.setBackground(UIUtils.WHITE);
        for (BarChartPanel c : charts) {
            chartGrid.add(c);
        }
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UIUtils.scroll(table), UIUtils.scroll(chartGrid));
        split.setResizeWeight(0.3);
        split.setBorder(null);
        body.add(split, BorderLayout.CENTER);
        fillCharts();
    }

    @Override
    public void onShow() {
        note.setText("Workload: " + app.getProcesses().size() + " process(es). Parameters (quantum, aging, queues, context switch) are taken from the Algorithms page.");
    }

    private void runComparison() {
        List<SchedulingAlgorithmType> selected = new ArrayList<>();
        for (Map.Entry<SchedulingAlgorithmType, JCheckBox> e : checks.entrySet()) {
            if (e.getValue().isSelected()) {
                selected.add(e.getKey());
            }
        }
        if (selected.isEmpty()) {
            UIUtils.error(this, "Please select at least one algorithm.");
            return;
        }
        try {
            show(service.compareAlgorithms(app.getProcesses(), app.getConfig(), selected));
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void runWhatIf() {
        try {
            String[] parts = quantumField.getText().split(",");
            int[] quanta = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                quanta[i] = InputValidator.parseInt(parts[i], "Each quantum");
            }
            show(service.compareQuantums(app.getProcesses(), app.getConfig(), quanta));
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void show(List<SimulationResult> newResults) {
        results = newResults;
        model.setRowCount(0);
        for (SimulationResult r : results) {
            MetricsSummary m = r.getMetrics();
            model.addRow(new Object[] {r.getLabel(), FormatUtils.decimal(m.getAvgWaitingTime()),
                FormatUtils.decimal(m.getAvgTurnaroundTime()), FormatUtils.decimal(m.getAvgResponseTime()),
                FormatUtils.percent(m.getCpuUtilization()), FormatUtils.decimal(m.getThroughput()),
                m.getContextSwitches(), m.getPreemptions()});
        }
        fillCharts();
    }

    private void fillCharts() {
        chart(0, "Average Waiting Time", "", false, m -> m.getAvgWaitingTime());
        chart(1, "Average Turnaround Time", "", false, m -> m.getAvgTurnaroundTime());
        chart(2, "Average Response Time", "", false, m -> m.getAvgResponseTime());
        chart(3, "CPU Utilization", "%", false, m -> m.getCpuUtilization());
        chart(4, "Context Switches", "", true, m -> m.getContextSwitches());
    }

    private void chart(int index, String title, String suffix, boolean integers, ToDoubleFunction<MetricsSummary> f) {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (SimulationResult r : results) {
            labels.add(r.getLabel());
            values.add(f.applyAsDouble(r.getMetrics()));
        }
        charts[index].setData(title, labels, values, suffix, integers);
    }

    private void export() {
        if (results.isEmpty()) {
            UIUtils.error(this, "Run a comparison first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("comparison.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            SimulationExporter.exportComparison(results, chooser.getSelectedFile());
            UIUtils.info(this, "Comparison exported.");
        } catch (IOException ex) {
            UIUtils.error(this, "Could not write the file: " + ex.getMessage());
        }
    }
}
