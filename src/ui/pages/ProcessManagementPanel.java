package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.ApplicationController;
import export.WorkloadFileService;
import generator.SampleWorkloads;
import model.Process;
import ui.components.ProcessTablePanel;
import ui.components.ThemedButton;
import ui.dialogs.AddProcessDialog;
import ui.dialogs.ConfirmationDialog;
import ui.dialogs.EditProcessDialog;
import ui.dialogs.GeneratorDialog;
import util.UIUtils;

/** Add / edit / delete / generate / import / save processes. */
public class ProcessManagementPanel extends BasePage {
    private static final long serialVersionUID = 1L;

    private final ProcessTablePanel table = new ProcessTablePanel(null, false);
    private final JLabel countLabel = UIUtils.muted(" ");
    private final JComboBox<String> samples = new JComboBox<>();

    public ProcessManagementPanel(ApplicationController app) {
        super(app, "Processes", "Create the workload that will be scheduled");
        for (String name : SampleWorkloads.all().keySet()) {
            samples.addItem(name);
        }

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(UIUtils.WHITE);
        toolbar.add(button("Add Process", ThemedButton.Kind.PRIMARY, this::onAdd));
        toolbar.add(button("Edit Selected", ThemedButton.Kind.SECONDARY, this::onEdit));
        toolbar.add(button("Delete Selected", ThemedButton.Kind.SECONDARY, this::onDelete));
        toolbar.add(button("Clear All", ThemedButton.Kind.DANGER, this::onClear));
        toolbar.add(button("Generate Random...", ThemedButton.Kind.SECONDARY, this::onGenerate));
        toolbar.add(button("Import CSV", ThemedButton.Kind.SECONDARY, this::onImport));
        toolbar.add(button("Save CSV", ThemedButton.Kind.SECONDARY, this::onSave));

        JPanel sampleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sampleBar.setBackground(UIUtils.WHITE);
        sampleBar.add(UIUtils.muted("Predefined test workload:"));
        sampleBar.add(samples);
        sampleBar.add(button("Load Sample", ThemedButton.Kind.SECONDARY, this::onLoadSample));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIUtils.WHITE);
        top.add(toolbar, BorderLayout.NORTH);
        top.add(sampleBar, BorderLayout.SOUTH);
        body.add(top, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        body.add(countLabel, BorderLayout.SOUTH);
        table.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEdit();
                }
            }
        });
    }

    private ThemedButton button(String text, ThemedButton.Kind kind, Runnable action) {
        ThemedButton b = new ThemedButton(text, kind);
        b.addActionListener(e -> action.run());
        return b;
    }

    private Window window() {
        return SwingUtilities.getWindowAncestor(this);
    }

    @Override
    public void onShow() {
        table.setProcesses(app.getProcesses());
        countLabel.setText(app.getProcesses().size() + " process(es) in the workload. Double-click a row to edit it.");
    }

    private void onAdd() {
        new AddProcessDialog(window(), app).setVisible(true);
        onShow();
    }

    private void onEdit() {
        Process p = table.getSelectedProcess();
        if (p == null) {
            UIUtils.info(this, "Please select a process in the table first.");
            return;
        }
        new EditProcessDialog(window(), app, p).setVisible(true);
        onShow();
    }

    private void onDelete() {
        Process p = table.getSelectedProcess();
        if (p == null) {
            UIUtils.info(this, "Please select a process in the table first.");
            return;
        }
        if (ConfirmationDialog.confirm(this, "Delete process " + p.getPid() + "?")) {
            app.removeProcess(p.getPid());
            onShow();
        }
    }

    private void onClear() {
        if (!app.getProcesses().isEmpty() && ConfirmationDialog.confirm(this, "Remove all processes?")) {
            app.clearProcesses();
            onShow();
        }
    }

    private void onGenerate() {
        new GeneratorDialog(window(), app).setVisible(true);
        onShow();
    }

    private void onLoadSample() {
        if (!app.getProcesses().isEmpty() && !ConfirmationDialog.confirm(this, "Replace the current workload with the sample?")) {
            return;
        }
        Supplier<List<Process>> supplier = SampleWorkloads.all().get((String) samples.getSelectedItem());
        app.replaceProcesses(supplier.get());
        onShow();
    }

    private void onImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            app.replaceProcesses(WorkloadFileService.load(chooser.getSelectedFile()));
            onShow();
        } catch (IllegalArgumentException | IOException ex) {
            UIUtils.error(this, "Could not import the file. " + ex.getMessage());
        }
    }

    private void onSave() {
        if (app.getProcesses().isEmpty()) {
            UIUtils.error(this, "Please add at least one process.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("workload.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            WorkloadFileService.save(app.getProcesses(), chooser.getSelectedFile());
            UIUtils.info(this, "Workload saved.");
        } catch (IOException ex) {
            UIUtils.error(this, "Could not save the file: " + ex.getMessage());
        }
    }
}
