package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import controller.ApplicationController;
import controller.SimulationController;
import model.MetricsSummary;
import model.SimulationState;
import simulation.SimulationEngine;
import ui.components.CpuPanel;
import ui.components.EventLogPanel;
import ui.components.GanttChartPanel;
import ui.components.MetricCard;
import ui.components.ProcessTablePanel;
import ui.components.ReadyQueuePanel;
import ui.components.ThemedButton;
import util.FormatUtils;
import util.UIUtils;

/**
 * Main page. It only DISPLAYS what the SimulationEngine reports; all control goes through the
 * SimulationController (timer, start/pause/step...).
 */
public class SimulationPanel extends BasePage implements SimulationController.Listener {
    private static final long serialVersionUID = 1L;
    private static final String[] SPEED_LABELS = {"0.5x", "1x", "2x", "5x", "10x"};
    private static final double[] SPEED_VALUES = {0.5, 1, 2, 5, 10};

    private final transient SimulationController controller;
    private final JLabel algorithmLabel = new JLabel("-");
    private final JLabel timeLabel = new JLabel("t = 0");
    private final JLabel stateLabel = new JLabel("NOT_STARTED");

    private final ThemedButton startBtn = new ThemedButton("Start", ThemedButton.Kind.PRIMARY);
    private final ThemedButton pauseBtn = new ThemedButton("Pause");
    private final ThemedButton resumeBtn = new ThemedButton("Resume");
    private final ThemedButton stepBtn = new ThemedButton("Step");
    private final ThemedButton stopBtn = new ThemedButton("Stop", ThemedButton.Kind.DANGER);
    private final ThemedButton resetBtn = new ThemedButton("Reset");
    private final ThemedButton restartBtn = new ThemedButton("Restart");
    private final ThemedButton endBtn = new ThemedButton("Run to End");
    private final JComboBox<String> speedBox = new JComboBox<>(SPEED_LABELS);

    private final CpuPanel cpuPanel = new CpuPanel();
    private final ReadyQueuePanel queuePanel = new ReadyQueuePanel();
    private final ProcessTablePanel stateTable = new ProcessTablePanel("Process States", true);
    private final EventLogPanel eventLog = new EventLogPanel();
    private final GanttChartPanel gantt = new GanttChartPanel();

    private final MetricCard avgWt = new MetricCard("Avg waiting");
    private final MetricCard avgTat = new MetricCard("Avg turnaround");
    private final MetricCard avgRt = new MetricCard("Avg response");
    private final MetricCard util = new MetricCard("CPU utilization");
    private final MetricCard switches = new MetricCard("Context switches");
    private final MetricCard preemptions = new MetricCard("Preemptions");

    public SimulationPanel(ApplicationController app) {
        super(app, "Simulation", "Live CPU scheduling - every visual comes from the simulation engine");
        this.controller = app.getSimulationController();
        controller.addListener(this);
        speedBox.setSelectedIndex(1);
        controller.setSpeed(1.0);

        body.add(buildControls(), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setBackground(UIUtils.WHITE);
        grid.add(cpuPanel);
        grid.add(queuePanel);
        grid.add(stateTable);
        grid.add(eventLog);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(UIUtils.WHITE);
        JLabel ganttTitle = UIUtils.title("Gantt Chart");
        bottom.add(ganttTitle, BorderLayout.NORTH);
        bottom.add(UIUtils.scroll(gantt), BorderLayout.CENTER);
        JPanel metrics = new JPanel(new GridLayout(1, 6, 8, 0));
        metrics.setBackground(UIUtils.WHITE);
        for (MetricCard c : new MetricCard[] {avgWt, avgTat, avgRt, util, switches, preemptions}) {
            metrics.add(c);
        }
        bottom.add(metrics, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grid, bottom);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setContinuousLayout(true);
        body.add(split, BorderLayout.CENTER);

        startBtn.addActionListener(e -> guarded(controller::start));
        pauseBtn.addActionListener(e -> controller.pause());
        resumeBtn.addActionListener(e -> controller.resume());
        stepBtn.addActionListener(e -> guarded(controller::step));
        stopBtn.addActionListener(e -> controller.stop());
        resetBtn.addActionListener(e -> controller.reset());
        restartBtn.addActionListener(e -> guarded(controller::restart));
        endBtn.addActionListener(e -> guarded(controller::runToEnd));
        speedBox.addActionListener(e -> controller.setSpeed(SPEED_VALUES[speedBox.getSelectedIndex()]));
    }

    private JPanel buildControls() {
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 2));
        info.setBackground(UIUtils.WHITE);
        algorithmLabel.setFont(UIUtils.font(Font.BOLD, 13));
        timeLabel.setFont(UIUtils.font(Font.BOLD, 13));
        timeLabel.setForeground(UIUtils.ACCENT_DARK);
        stateLabel.setFont(UIUtils.font(Font.BOLD, 12));
        info.add(algorithmLabel);
        info.add(timeLabel);
        info.add(stateLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        buttons.setBackground(UIUtils.WHITE);
        for (ThemedButton b : new ThemedButton[] {startBtn, pauseBtn, resumeBtn, stepBtn, stopBtn, resetBtn, restartBtn, endBtn}) {
            buttons.add(b);
        }
        buttons.add(UIUtils.muted("  Speed:"));
        buttons.add(speedBox);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtils.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        p.add(info, BorderLayout.NORTH);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private void guarded(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    @Override
    public void onShow() {
        controller.prepare();
        refresh();
    }

    @Override
    public void simulationUpdated() {
        refresh();
    }

    private void refresh() {
        SimulationEngine e = controller.getEngine();
        if (e == null) {
            showEmpty();
            return;
        }
        algorithmLabel.setText(e.getConfig().describe());
        timeLabel.setText("t = " + e.getCurrentTime());
        stateLabel.setText(e.getStatus().toString());
        stateLabel.setForeground(e.getStatus() == SimulationState.RUNNING ? UIUtils.SUCCESS
                : e.getStatus() == SimulationState.COMPLETED ? UIUtils.ACCENT_DARK : UIUtils.MUTED);
        cpuPanel.update(e.getCpu(), e.getCurrentTime(), e.getQuantumOfRunning(), e.getScheduler().getName());
        queuePanel.setData(e.getScheduler().queueNames(), e.getScheduler().queueSnapshot(),
                e.getScheduler().getDataStructureDescription());
        stateTable.setProcesses(e.getProcesses());
        eventLog.sync(e.getEvents());
        gantt.setData(e.getSegments(), e.getCurrentTime(), !e.isFinished());
        MetricsSummary m = e.getLiveMetrics();
        avgWt.setValue(FormatUtils.decimal(m.getAvgWaitingTime()));
        avgTat.setValue(FormatUtils.decimal(m.getAvgTurnaroundTime()));
        avgRt.setValue(FormatUtils.decimal(m.getAvgResponseTime()));
        util.setValue(FormatUtils.percent(m.getCpuUtilization()));
        switches.setValue(String.valueOf(m.getContextSwitches()));
        preemptions.setValue(String.valueOf(m.getPreemptions()));
        updateButtons(e.getStatus());
    }

    private void showEmpty() {
        algorithmLabel.setText(app.getConfig().describe());
        timeLabel.setText("t = 0");
        stateLabel.setText("NOT_STARTED (add processes first)");
        stateLabel.setForeground(UIUtils.MUTED);
        stateTable.setProcesses(app.getProcesses());
        queuePanel.setData(new java.util.ArrayList<>(), new java.util.ArrayList<>(), " ");
        gantt.setData(new java.util.ArrayList<>(), 0, false);
        eventLog.clear();
        for (MetricCard c : new MetricCard[] {avgWt, avgTat, avgRt, util, switches, preemptions}) {
            c.setValue("-");
        }
        startBtn.setEnabled(true);
        for (ThemedButton b : new ThemedButton[] {pauseBtn, resumeBtn, stepBtn, stopBtn, resetBtn, endBtn}) {
            b.setEnabled(false);
        }
        restartBtn.setEnabled(false);
    }

    private void updateButtons(SimulationState s) {
        boolean notStarted = s == SimulationState.NOT_STARTED;
        boolean running = s == SimulationState.RUNNING;
        boolean paused = s == SimulationState.PAUSED;
        boolean finished = s == SimulationState.COMPLETED || s == SimulationState.STOPPED;
        startBtn.setEnabled(notStarted || finished);
        pauseBtn.setEnabled(running);
        resumeBtn.setEnabled(paused);
        stepBtn.setEnabled(notStarted || paused || running);
        stopBtn.setEnabled(running || paused);
        resetBtn.setEnabled(true);
        restartBtn.setEnabled(true);
        endBtn.setEnabled(notStarted || paused || running);
    }
}
