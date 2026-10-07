package ui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import model.QueueLevelConfig;
import model.QueuePolicy;
import util.Constants;
import util.UIUtils;

/** Editor for the list of queue levels of MLQ / MLFQ: count, and per level name, policy, quantum. */
public class LevelConfigEditor extends JPanel {
    private static final long serialVersionUID = 1L;

    private final QueuePolicy[] allowed;
    private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(3, 1, Constants.MAX_QUEUE_LEVELS, 1));
    private final JPanel rowsPanel = new JPanel();
    private final transient List<Row> rows = new ArrayList<>();
    private boolean rebuilding;

    public LevelConfigEditor(String title, QueuePolicy[] allowedPolicies) {
        super(new BorderLayout(0, 6));
        this.allowed = allowedPolicies;
        setBackground(UIUtils.WHITE);
        setBorder(BorderFactory.createTitledBorder(title));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        top.setBackground(UIUtils.WHITE);
        top.add(new JLabel("Number of queues:"));
        top.add(countSpinner);
        add(top, BorderLayout.NORTH);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(UIUtils.WHITE);
        add(rowsPanel, BorderLayout.CENTER);
        countSpinner.addChangeListener(e -> {
            if (!rebuilding) {
                resize((Integer) countSpinner.getValue());
            }
        });
    }

    public void setLevels(List<QueueLevelConfig> levels) {
        rebuilding = true;
        countSpinner.setValue(levels.size());
        rowsPanel.removeAll();
        rows.clear();
        for (QueueLevelConfig l : levels) {
            addRow(l);
        }
        rebuilding = false;
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    public List<QueueLevelConfig> getLevels() {
        List<QueueLevelConfig> out = new ArrayList<>();
        for (Row r : rows) {
            out.add(r.toConfig());
        }
        return out;
    }

    private void resize(int count) {
        List<QueueLevelConfig> current = getLevels();
        while (current.size() > count) {
            current.remove(current.size() - 1);
        }
        while (current.size() < count) {
            current.add(new QueueLevelConfig("Level " + (current.size() + 1), allowed[0], 4));
        }
        setLevels(current);
    }

    private void addRow(QueueLevelConfig config) {
        Row r = new Row(rows.size() + 1, config);
        rows.add(r);
        rowsPanel.add(r.panel);
    }

    private class Row {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        private final JTextField name = new JTextField(10);
        private final JComboBox<QueuePolicy> policy = new JComboBox<>(allowed);
        private final JSpinner quantum = new JSpinner(new SpinnerNumberModel(2, 1, 10000, 1));

        Row(int index, QueueLevelConfig c) {
            panel.setBackground(UIUtils.WHITE);
            name.setText(c.getName());
            policy.setSelectedItem(c.getPolicy());
            quantum.setValue(Math.max(1, c.getQuantum()));
            quantum.setEnabled(c.getPolicy() == QueuePolicy.ROUND_ROBIN);
            policy.addActionListener(e -> quantum.setEnabled(policy.getSelectedItem() == QueuePolicy.ROUND_ROBIN));
            panel.add(new JLabel("Queue " + index + ":"));
            panel.add(name);
            panel.add(policy);
            panel.add(new JLabel("quantum"));
            panel.add(quantum);
        }

        QueueLevelConfig toConfig() {
            String n = name.getText().trim().isEmpty() ? "Level" : name.getText().trim();
            return new QueueLevelConfig(n, (QueuePolicy) policy.getSelectedItem(), (Integer) quantum.getValue());
        }
    }
}
