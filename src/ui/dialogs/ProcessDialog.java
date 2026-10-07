package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import model.Process;
import ui.components.ThemedButton;
import util.Constants;
import util.UIUtils;
import validation.InputValidator;

/** Shared form of the Add / Edit process dialogs. Subclasses decide what happens on Save. */
public abstract class ProcessDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JTextField pidField = new JTextField(12);
    private final JTextField nameField = new JTextField(12);
    private final JTextField arrivalField = new JTextField(12);
    private final JTextField burstField = new JTextField(12);
    private final JTextField priorityField = new JTextField(12);
    private final JSpinner queueSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Constants.MAX_QUEUE_LEVELS, 1));

    protected ProcessDialog(Window owner, String title, Process initial) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        pidField.setText(initial.getPid());
        nameField.setText(initial.getName());
        arrivalField.setText(String.valueOf(initial.getArrivalTime()));
        burstField.setText(String.valueOf(initial.getBurstTime()));
        priorityField.setText(String.valueOf(initial.getPriority()));
        queueSpinner.setValue(initial.getQueueLevel());

        JPanel form = UIUtils.formPanel();
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        UIUtils.addFormRow(form, 0, "PID", pidField);
        UIUtils.addFormRow(form, 1, "Name", nameField);
        UIUtils.addFormRow(form, 2, "Arrival time (>= 0)", arrivalField);
        UIUtils.addFormRow(form, 3, "Burst time (> 0)", burstField);
        UIUtils.addFormRow(form, 4, "Priority (0 - " + Constants.MAX_PRIORITY + ")", priorityField);
        UIUtils.addFormRow(form, 5, "Queue / class (MLQ)", queueSpinner);

        ThemedButton save = new ThemedButton("Save", ThemedButton.Kind.PRIMARY);
        ThemedButton cancel = new ThemedButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(UIUtils.WHITE);
        buttons.add(cancel);
        buttons.add(save);

        getContentPane().setBackground(UIUtils.WHITE);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onSave() {
        try {
            Process p = new Process(pidField.getText().trim(), nameField.getText(),
                    InputValidator.parseInt(arrivalField.getText(), "Arrival time"),
                    InputValidator.parseInt(burstField.getText(), "Burst time"),
                    InputValidator.parseInt(priorityField.getText(), "Priority"),
                    (Integer) queueSpinner.getValue());
            commit(p);
            dispose();
        } catch (IllegalArgumentException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    /** Validate and store the process; throw IllegalArgumentException to keep the dialog open. */
    protected abstract void commit(Process p);
}
