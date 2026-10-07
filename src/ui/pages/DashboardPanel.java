package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.ApplicationController;
import controller.PageId;
import model.MetricsSummary;
import model.SchedulingAlgorithmType;
import model.SimulationResult;
import ui.components.MetricCard;
import ui.components.ThemedButton;
import util.FormatUtils;
import util.UIUtils;

/** Landing page: key numbers, quick actions and a short "how to start" guide. */
public class DashboardPanel extends BasePage {
    private static final long serialVersionUID = 1L;

    private final MetricCard processes = new MetricCard("Processes in workload");
    private final MetricCard algorithms = new MetricCard("Available algorithms");
    private final MetricCard lastRun = new MetricCard("Last simulation");
    private final MetricCard avgWt = new MetricCard("Avg waiting time (last)");
    private final MetricCard util = new MetricCard("CPU utilization (last)");
    private final JLabel configLabel = UIUtils.muted(" ");

    public DashboardPanel(ApplicationController app) {
        super(app, "Dashboard", "Overview of your workload and the latest simulation");
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setBackground(UIUtils.WHITE);
        cards.add(processes);
        cards.add(algorithms);
        cards.add(lastRun);
        cards.add(avgWt);
        cards.add(util);
        body.add(cards, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UIUtils.WHITE);

        JPanel actions = UIUtils.card(new BorderLayout(0, 10));
        actions.add(UIUtils.title("Quick actions"), BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setBackground(UIUtils.WHITE);
        buttons.add(action("New Simulation", PageId.SIMULATION, ThemedButton.Kind.PRIMARY));
        buttons.add(action("Manage Processes", PageId.PROCESSES, ThemedButton.Kind.SECONDARY));
        buttons.add(action("Configure Algorithm", PageId.ALGORITHMS, ThemedButton.Kind.SECONDARY));
        buttons.add(action("Compare Algorithms", PageId.COMPARE, ThemedButton.Kind.SECONDARY));
        buttons.add(action("View History", PageId.HISTORY, ThemedButton.Kind.SECONDARY));
        actions.add(buttons, BorderLayout.CENTER);
        actions.add(configLabel, BorderLayout.SOUTH);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        center.add(actions);
        center.add(javax.swing.Box.createVerticalStrut(12));

        JPanel guide = UIUtils.card(new BorderLayout(0, 8));
        guide.add(UIUtils.title("How to run your first simulation"), BorderLayout.NORTH);
        JPanel steps = new JPanel(new GridLayout(0, 1, 0, 4));
        steps.setBackground(UIUtils.WHITE);
        steps.add(new JLabel("1.  Processes: add processes, generate a random workload or load a sample test case."));
        steps.add(new JLabel("2.  Algorithms: choose an algorithm and its parameters (quantum, aging, queues, context switch)."));
        steps.add(new JLabel("3.  Simulation: press Start (or Step) and watch the CPU, ready queue, Gantt chart and event log."));
        steps.add(new JLabel("4.  Results: inspect per-process metrics and export them as CSV."));
        steps.add(new JLabel("5.  Compare: run the same workload through several algorithms and compare the charts."));
        guide.add(steps, BorderLayout.CENTER);
        guide.setAlignmentX(LEFT_ALIGNMENT);
        center.add(guide);
        body.add(center, BorderLayout.CENTER);
    }

    private ThemedButton action(String text, PageId target, ThemedButton.Kind kind) {
        ThemedButton b = new ThemedButton(text, kind);
        b.addActionListener(e -> app.navigateTo(target));
        return b;
    }

    @Override
    public void onShow() {
        processes.setValue(String.valueOf(app.getProcesses().size()));
        algorithms.setValue(String.valueOf(SchedulingAlgorithmType.values().length));
        SimulationResult r = app.getLastResult();
        if (r == null) {
            lastRun.setValue("none yet");
            avgWt.setValue("-");
            util.setValue("-");
        } else {
            MetricsSummary m = r.getMetrics();
            lastRun.setValue(r.getLabel());
            avgWt.setValue(FormatUtils.decimal(m.getAvgWaitingTime()));
            util.setValue(FormatUtils.percent(m.getCpuUtilization()));
        }
        configLabel.setText("Selected configuration: " + app.getConfig().describe());
        configLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    }
}
