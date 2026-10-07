package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import controller.ApplicationController;
import controller.PageId;
import model.QueuePolicy;
import model.SchedulingAlgorithmType;
import model.SimulationConfig;
import ui.components.LevelConfigEditor;
import ui.components.ThemedButton;
import util.UIUtils;
import validation.InputValidator;

/** Choose the algorithm and show only the parameters that algorithm needs. */
public class AlgorithmConfigurationPanel extends BasePage {
    private static final long serialVersionUID = 1L;
    private static final String LOWER = "Lower number = higher priority";
    private static final String HIGHER = "Higher number = higher priority";

    private final JComboBox<SchedulingAlgorithmType> algorithmBox = new JComboBox<>(SchedulingAlgorithmType.values());
    private final JLabel description = UIUtils.muted(" ");
    private final JSpinner quantum = new JSpinner(new SpinnerNumberModel(2, 1, 10000, 1));
    private final JComboBox<String> direction = new JComboBox<>(new String[] {LOWER, HIGHER});
    private final JCheckBox aging = new JCheckBox("Enable aging (starvation prevention)");
    private final JSpinner agingInterval = new JSpinner(new SpinnerNumberModel(5, 1, 10000, 1));
    private final JCheckBox contextSwitch = new JCheckBox("Enable context switch cost");
    private final JSpinner contextCost = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
    private final JSpinner starvation = new JSpinner(new SpinnerNumberModel(30, 0, 100000, 1));
    private final LevelConfigEditor mlqEditor = new LevelConfigEditor("Multilevel Queue - queues (Queue 1 has the highest priority)",
            new QueuePolicy[] {QueuePolicy.FCFS, QueuePolicy.ROUND_ROBIN, QueuePolicy.PRIORITY});
    private final LevelConfigEditor mlfqEditor = new LevelConfigEditor("MLFQ - levels (new processes start in level 1)",
            new QueuePolicy[] {QueuePolicy.ROUND_ROBIN, QueuePolicy.FCFS});

    private final JPanel rrPanel = section("Round Robin");
    private final JPanel priorityPanel = section("Priority direction");
    private final JPanel agingPanel = section("Aging");

    public AlgorithmConfigurationPanel(ApplicationController app) {
        super(app, "Algorithm Configuration", "Select a scheduling algorithm and its parameters");

        JPanel selector = UIUtils.card(new BorderLayout(0, 6));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UIUtils.WHITE);
        row.add(UIUtils.title("Algorithm:"));
        row.add(algorithmBox);
        selector.add(row, BorderLayout.NORTH);
        selector.add(description, BorderLayout.SOUTH);

        rrPanel.add(labelled("Time quantum (> 0):", quantum));
        priorityPanel.add(labelled("Direction:", direction));
        agingPanel.add(aging);
        agingPanel.add(labelled("Aging interval / promotion threshold (time units waited):", agingInterval));
        aging.setBackground(UIUtils.WHITE);
        contextSwitch.setBackground(UIUtils.WHITE);

        JPanel common = section("Common settings");
        common.add(contextSwitch);
        common.add(labelled("Context switch cost (time units):", contextCost));
        common.add(labelled("Starvation warning threshold (0 = off):", starvation));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBackground(UIUtils.WHITE);
        for (JPanel p : new JPanel[] {selector, rrPanel, priorityPanel, agingPanel, mlqEditor, mlfqEditor, common}) {
            p.setAlignmentX(LEFT_ALIGNMENT);
            stack.add(p);
            stack.add(javax.swing.Box.createVerticalStrut(10));
        }
        body.add(UIUtils.scroll(stack), BorderLayout.CENTER);

        ThemedButton apply = new ThemedButton("Apply Configuration", ThemedButton.Kind.PRIMARY);
        ThemedButton defaults = new ThemedButton("Reset to Defaults");
        ThemedButton simulate = new ThemedButton("Go to Simulation");
        apply.addActionListener(e -> apply(false));
        defaults.addActionListener(e -> {
            app.setConfig(new SimulationConfig());
            load();
        });
        simulate.addActionListener(e -> apply(true));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setBackground(UIUtils.WHITE);
        buttons.add(apply);
        buttons.add(defaults);
        buttons.add(simulate);
        body.add(buttons, BorderLayout.SOUTH);

        algorithmBox.addActionListener(e -> updateVisibility());
    }

    private static JPanel section(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIUtils.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private static JPanel labelled(String label, javax.swing.JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setBackground(UIUtils.WHITE);
        p.add(new JLabel(label));
        p.add(c);
        return p;
    }

    private void updateVisibility() {
        SchedulingAlgorithmType t = (SchedulingAlgorithmType) algorithmBox.getSelectedItem();
        description.setText(t.getDescription());
        rrPanel.setVisible(t == SchedulingAlgorithmType.ROUND_ROBIN);
        priorityPanel.setVisible(t == SchedulingAlgorithmType.PRIORITY || t == SchedulingAlgorithmType.MLQ);
        agingPanel.setVisible(t == SchedulingAlgorithmType.PRIORITY || t == SchedulingAlgorithmType.MLFQ);
        mlqEditor.setVisible(t == SchedulingAlgorithmType.MLQ);
        mlfqEditor.setVisible(t == SchedulingAlgorithmType.MLFQ);
        revalidate();
        repaint();
    }

    private void load() {
        SimulationConfig c = app.getConfig();
        algorithmBox.setSelectedItem(c.getAlgorithm());
        quantum.setValue(Math.max(1, c.getTimeQuantum()));
        direction.setSelectedItem(c.isPriorityLowerIsHigher() ? LOWER : HIGHER);
        aging.setSelected(c.isAgingEnabled());
        agingInterval.setValue(Math.max(1, c.getAgingInterval()));
        contextSwitch.setSelected(c.isContextSwitchEnabled());
        contextCost.setValue(Math.max(0, c.getContextSwitchCost()));
        starvation.setValue(Math.max(0, c.getStarvationThreshold()));
        mlqEditor.setLevels(c.copy().getMlqLevels());
        mlfqEditor.setLevels(c.copy().getMlfqLevels());
        updateVisibility();
    }

    private void apply(boolean thenSimulate) {
        SimulationConfig c = new SimulationConfig();
        c.setAlgorithm((SchedulingAlgorithmType) algorithmBox.getSelectedItem());
        c.setTimeQuantum((Integer) quantum.getValue());
        c.setPriorityLowerIsHigher(LOWER.equals(direction.getSelectedItem()));
        c.setAgingEnabled(aging.isSelected());
        c.setAgingInterval((Integer) agingInterval.getValue());
        c.setContextSwitchEnabled(contextSwitch.isSelected());
        c.setContextSwitchCost((Integer) contextCost.getValue());
        c.setStarvationThreshold((Integer) starvation.getValue());
        c.setMlqLevels(mlqEditor.getLevels());
        c.setMlfqLevels(mlfqEditor.getLevels());
        try {
            InputValidator.validateConfig(c);
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
            return;
        }
        app.setConfig(c);
        if (thenSimulate) {
            app.navigateTo(PageId.SIMULATION);
        } else {
            UIUtils.info(this, "Configuration applied:\n" + c.describe());
        }
    }

    @Override
    public void onShow() {
        load();
    }
}
