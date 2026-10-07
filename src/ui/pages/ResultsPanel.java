package ui.pages;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import controller.ApplicationController;
import controller.PageId;
import export.SimulationExporter;
import model.MetricsSummary;
import model.Process;
import model.SimulationResult;
import ui.components.GanttChartPanel;
import ui.components.MetricCard;
import ui.components.ThemedButton;
import util.FormatUtils;
import util.UIUtils;

/** Per-process results table, summary metrics, final Gantt chart and CSV export. */
public class ResultsPanel extends BasePage {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"PID", "Name", "Arrival", "Burst", "Priority", "Start", "Completion",
        "Turnaround", "Waiting", "Response", "Preemptions", "Context Switches"};

    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);
    private final JLabel headline = UIUtils.title(" ");
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final GanttChartPanel gantt = new GanttChartPanel();
    private final MetricCard[] cardsArray = {
        new MetricCard("Average waiting time"), new MetricCard("Average turnaround time"),
        new MetricCard("Average response time"), new MetricCard("CPU utilization"),
        new MetricCard("Throughput (proc/unit)"), new MetricCard("Total time"),
        new MetricCard("Idle time"), new MetricCard("Context switches / preemptions")
    };
    private transient SimulationResult shown;

    public ResultsPanel(ApplicationController app) {
        super(app, "Results", "Metrics of the most recent completed simulation");

        JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 80));
        empty.setBackground(UIUtils.WHITE);
        ThemedButton go = new ThemedButton("Go to Simulation", ThemedButton.Kind.PRIMARY);
        go.addActionListener(e -> app.navigateTo(PageId.SIMULATION));
        empty.add(UIUtils.muted("No completed simulation yet. Run one to see results here."));
        empty.add(go);

        JPanel summary = new JPanel(new GridLayout(2, 4, 8, 8));
        summary.setBackground(UIUtils.WHITE);
        for (MetricCard c : cardsArray) {
            summary.add(c);
        }
        JTable table = new JTable(model);
        UIUtils.styleTable(table);
        JPanel ganttBox = new JPanel(new BorderLayout(0, 6));
        ganttBox.setBackground(UIUtils.WHITE);
        ganttBox.add(UIUtils.title("Gantt Chart"), BorderLayout.NORTH);
        ganttBox.add(UIUtils.scroll(gantt), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, UIUtils.scroll(table), ganttBox);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        ThemedButton export = new ThemedButton("Export CSV", ThemedButton.Kind.PRIMARY);
        export.addActionListener(e -> onExport());
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIUtils.WHITE);
        top.add(headline, BorderLayout.WEST);
        top.add(export, BorderLayout.EAST);

        JPanel resultView = new JPanel(new BorderLayout(0, 10));
        resultView.setBackground(UIUtils.WHITE);
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setBackground(UIUtils.WHITE);
        north.add(top, BorderLayout.NORTH);
        north.add(summary, BorderLayout.CENTER);
        resultView.add(north, BorderLayout.NORTH);
        resultView.add(split, BorderLayout.CENTER);

        cardHost.setBackground(UIUtils.WHITE);
        cardHost.add(empty, "empty");
        cardHost.add(resultView, "result");
        body.add(cardHost, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        shown = app.getLastResult();
        if (shown == null) {
            cards.show(cardHost, "empty");
            return;
        }
        headline.setText(shown.getConfig().describe() + "   |   finished " + FormatUtils.dateTime(shown.getFinishedAt()));
        model.setRowCount(0);
        for (Process p : shown.getProcesses()) {
            model.addRow(new Object[] {p.getPid(), p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                p.getFirstStartTime(), p.getCompletionTime(), p.getTurnaroundTime(), p.getWaitingTime(),
                p.getResponseTime(), p.getPreemptions(), p.getContextSwitches()});
        }
        MetricsSummary m = shown.getMetrics();
        cardsArray[0].setValue(FormatUtils.decimal(m.getAvgWaitingTime()));
        cardsArray[1].setValue(FormatUtils.decimal(m.getAvgTurnaroundTime()));
        cardsArray[2].setValue(FormatUtils.decimal(m.getAvgResponseTime()));
        cardsArray[3].setValue(FormatUtils.percent(m.getCpuUtilization()));
        cardsArray[4].setValue(FormatUtils.decimal(m.getThroughput()));
        cardsArray[5].setValue(String.valueOf(m.getTotalTime()));
        cardsArray[6].setValue(String.valueOf(m.getIdleTime()));
        cardsArray[7].setValue(m.getContextSwitches() + " / " + m.getPreemptions());
        gantt.setData(shown.getSegments(), m.getTotalTime(), false);
        cards.show(cardHost, "result");
    }

    private void onExport() {
        if (shown == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("simulation_result.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            SimulationExporter.exportResult(shown, chooser.getSelectedFile());
            UIUtils.info(this, "Results exported to " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            UIUtils.error(this, "Could not write the file: " + ex.getMessage());
        }
    }
}
