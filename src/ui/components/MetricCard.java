package ui.components;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import util.UIUtils;

/** Small card showing a caption and one big value. */
public class MetricCard extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel valueLabel = new JLabel("-", SwingConstants.LEFT);

    public MetricCard(String caption) {
        super(new BorderLayout(0, 4));
        setBackground(UIUtils.WHITE);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UIUtils.BORDER),
                javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel cap = UIUtils.muted(caption);
        valueLabel.setFont(UIUtils.font(Font.BOLD, 20));
        valueLabel.setForeground(UIUtils.ACCENT_DARK);
        add(cap, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
