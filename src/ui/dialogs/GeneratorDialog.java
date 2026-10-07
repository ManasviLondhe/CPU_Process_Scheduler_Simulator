package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import controller.ApplicationController;
import generator.ProcessGenerator;
import generator.WorkloadPreset;
import model.Process;
import ui.components.ThemedButton;
import util.UIUtils;
import validation.InputValidator;

/** Random workload generator dialog (custom ranges or a preset). */
public class GeneratorDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String CUSTOM = "Custom (use the ranges below)";

    private final transient ApplicationController app;
    private final JComboBox<Object> presetBox = new JComboBox<>();
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
    private final JSpinner minArrival = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JSpinner maxArrival = new JSpinner(new SpinnerNumberModel(15, 0, 100000, 1));
    private final JSpinner minBurst = new JSpinner(new SpinnerNumberModel(2, 1, 100000, 1));
    private final JSpinner maxBurst = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 1));
    private final JSpinner minPriority = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
    private final JSpinner maxPriority = new JSpinner(new SpinnerNumberModel(5, 0, 1000, 1));
    private final JTextField seed = new JTextField(10);
    private final JCheckBox replace = new JCheckBox("Replace existing processes", true);

    public GeneratorDialog(Window owner, ApplicationController app) {
        super(owner, "Generate Random Processes", ModalityType.APPLICATION_MODAL);
        this.app = app;
        presetBox.addItem(CUSTOM);
        for (WorkloadPreset p : WorkloadPreset.values()) {
            presetBox.addItem(p);
        }
        presetBox.addActionListener(e -> onPresetChanged());
        replace.setBackground(UIUtils.WHITE);
        seed.setToolTipText("Optional. The same seed always produces the same workload.");

        JPanel form = UIUtils.formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        int r = 0;
        UIUtils.addFormRow(form, r++, "Preset", presetBox);
        UIUtils.addFormRow(form, r++, "Number of processes (1-100)", count);
        UIUtils.addFormRow(form, r++, "Arrival time - min", minArrival);
        UIUtils.addFormRow(form, r++, "Arrival time - max", maxArrival);
        UIUtils.addFormRow(form, r++, "Burst time - min", minBurst);
        UIUtils.addFormRow(form, r++, "Burst time - max", maxBurst);
        UIUtils.addFormRow(form, r++, "Priority - min", minPriority);
        UIUtils.addFormRow(form, r++, "Priority - max", maxPriority);
        UIUtils.addFormRow(form, r++, "Random seed (optional)", seed);
        UIUtils.addFormRow(form, r, "", replace);

        ThemedButton generate = new ThemedButton("Generate", ThemedButton.Kind.PRIMARY);
        ThemedButton cancel = new ThemedButton("Cancel");
        generate.addActionListener(e -> onGenerate());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(UIUtils.WHITE);
        buttons.add(cancel);
        buttons.add(generate);
        getContentPane().setBackground(UIUtils.WHITE);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onPresetChanged() {
        Object sel = presetBox.getSelectedItem();
        boolean custom = !(sel instanceof WorkloadPreset);
        JSpinner[] all = {count, minArrival, maxArrival, minBurst, maxBurst, minPriority, maxPriority};
        for (JSpinner s : all) {
            s.setEnabled(custom);
        }
        if (!custom) {
            WorkloadPreset p = (WorkloadPreset) sel;
            count.setValue(p.getCount());
            minArrival.setValue(0);
            maxArrival.setValue(p.getMaxArrival());
            minBurst.setValue(p.getMinBurst());
            maxBurst.setValue(p.getMaxBurst());
            minPriority.setValue(1);
            maxPriority.setValue(p.getMaxPriority());
        }
    }

    private void onGenerate() {
        try {
            Long seedValue = seed.getText().trim().isEmpty() ? null : Long.valueOf(InputValidator.parseInt(seed.getText(), "Seed"));
            ProcessGenerator gen = new ProcessGenerator();
            int start = replace.isSelected() ? 1 : app.nextPidNumber();
            Object sel = presetBox.getSelectedItem();
            List<Process> list;
            if (sel instanceof WorkloadPreset) {
                list = gen.generatePreset((WorkloadPreset) sel, seedValue, start);
            } else {
                list = gen.generate((Integer) count.getValue(), (Integer) minArrival.getValue(), (Integer) maxArrival.getValue(),
                        (Integer) minBurst.getValue(), (Integer) maxBurst.getValue(), (Integer) minPriority.getValue(),
                        (Integer) maxPriority.getValue(), 3, seedValue, start);
            }
            if (replace.isSelected()) {
                app.replaceProcesses(list);
            } else {
                app.appendProcesses(list);
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }
}
