package ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import model.Process;
import model.ProcessState;
import util.UIUtils;

/**
 * Table of processes. "live" mode shows state / remaining / waiting (simulation page);
 * the other mode shows only the input fields (process management page).
 */
public class ProcessTablePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String[] LIVE_COLUMNS = {"PID", "Name", "State", "Arrival", "Burst", "Remaining", "Priority", "Queue", "Waiting"};
    private static final String[] INPUT_COLUMNS = {"PID", "Name", "Arrival", "Burst", "Priority", "Queue"};

    private final boolean live;
    private final Model model = new Model();
    private final JTable table = new JTable(model);

    public ProcessTablePanel(String title, boolean live) {
        super(new BorderLayout());
        this.live = live;
        setBackground(UIUtils.WHITE);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (live) {
            table.getColumnModel().getColumn(2).setCellRenderer(new StateRenderer());
        }
        if (title != null) {
            JLabel l = UIUtils.title(title);
            l.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 4, 6, 4));
            add(l, BorderLayout.NORTH);
        }
        add(UIUtils.scroll(table), BorderLayout.CENTER);
    }

    public void setProcesses(List<Process> processes) {
        int selected = table.getSelectedRow();
        model.rows = new ArrayList<>(processes);
        model.fireTableDataChanged();
        if (selected >= 0 && selected < model.rows.size()) {
            table.setRowSelectionInterval(selected, selected);
        }
    }

    public Process getSelectedProcess() {
        int row = table.getSelectedRow();
        return row < 0 || row >= model.rows.size() ? null : model.rows.get(row);
    }

    public JTable getTable() {
        return table;
    }

    private class Model extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private List<Process> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return live ? LIVE_COLUMNS.length : INPUT_COLUMNS.length;
        }

        @Override
        public String getColumnName(int c) {
            return live ? LIVE_COLUMNS[c] : INPUT_COLUMNS[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Process p = rows.get(r);
            if (live) {
                switch (c) {
                    case 0: return p.getPid();
                    case 1: return p.getName();
                    case 2: return p.getState();
                    case 3: return p.getArrivalTime();
                    case 4: return p.getBurstTime();
                    case 5: return p.getRemainingTime();
                    case 6: return p.getEffectivePriority();
                    case 7: return p.getCurrentQueueLevel();
                    default: return p.getWaitingTime();
                }
            }
            switch (c) {
                case 0: return p.getPid();
                case 1: return p.getName();
                case 2: return p.getArrivalTime();
                case 3: return p.getBurstTime();
                case 4: return p.getPriority();
                default: return p.getQueueLevel();
            }
        }
    }

    private static class StateRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            if (value instanceof ProcessState) {
                l.setForeground(UIUtils.stateColor((ProcessState) value));
                l.setFont(l.getFont().deriveFont(Font.BOLD));
            }
            return l;
        }
    }
}
