package ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import controller.ApplicationController;
import controller.PageId;
import history.HistoryEntry;
import ui.components.ThemedButton;
import ui.dialogs.ConfirmationDialog;
import util.FormatUtils;
import util.UIUtils;

/** Simulations completed during this session. */
public class HistoryPanel extends BasePage {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"Date / Time", "Algorithm", "Processes", "Avg WT", "Avg TAT", "Avg RT", "CPU Util."};

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public HistoryPanel(ApplicationController app) {
        super(app, "History", "Simulations completed in this session");
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);

        ThemedButton view = new ThemedButton("View in Results", ThemedButton.Kind.PRIMARY);
        ThemedButton export = new ThemedButton("Export CSV");
        ThemedButton clear = new ThemedButton("Clear History", ThemedButton.Kind.DANGER);
        view.addActionListener(e -> onView());
        export.addActionListener(e -> onExport());
        clear.addActionListener(e -> onClear());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setBackground(UIUtils.WHITE);
        buttons.add(view);
        buttons.add(export);
        buttons.add(clear);
        body.add(buttons, BorderLayout.NORTH);
        body.add(UIUtils.scroll(table), BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        model.setRowCount(0);
        for (HistoryEntry e : app.getHistory().getEntries()) {
            model.addRow(new Object[] {FormatUtils.dateTime(e.getTime()), e.getAlgorithm(), e.getProcessCount(),
                FormatUtils.decimal(e.getMetrics().getAvgWaitingTime()), FormatUtils.decimal(e.getMetrics().getAvgTurnaroundTime()),
                FormatUtils.decimal(e.getMetrics().getAvgResponseTime()), FormatUtils.percent(e.getMetrics().getCpuUtilization())});
        }
    }

    private void onView() {
        int row = table.getSelectedRow();
        List<HistoryEntry> entries = app.getHistory().getEntries();
        if (row < 0 || row >= entries.size()) {
            UIUtils.info(this, "Please select a history row first.");
            return;
        }
        app.setLastResult(entries.get(row).getResult());
        app.navigateTo(PageId.RESULTS);
    }

    private void onExport() {
        if (app.getHistory().getEntries().isEmpty()) {
            UIUtils.info(this, "The history is empty.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("history.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            app.getHistory().exportCsv(chooser.getSelectedFile());
            UIUtils.info(this, "History exported.");
        } catch (IOException ex) {
            UIUtils.error(this, "Could not write the file: " + ex.getMessage());
        }
    }

    private void onClear() {
        if (!app.getHistory().getEntries().isEmpty() && ConfirmationDialog.confirm(this, "Delete the whole history?")) {
            app.getHistory().clear();
            onShow();
        }
    }
}
