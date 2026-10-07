package ui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.PageId;
import util.Constants;
import util.UIUtils;

/** Left sidebar with one button per page. */
public class NavigationPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Map<PageId, NavButton> buttons = new EnumMap<>(PageId.class);

    public NavigationPanel(Consumer<PageId> onSelect) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIUtils.PANEL_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.BORDER));
        setPreferredSize(new Dimension(210, 600));

        JLabel logo = new JLabel("\u2699  CPU Scheduler Lab");
        logo.setFont(UIUtils.font(Font.BOLD, 15));
        logo.setForeground(UIUtils.ACCENT_DARK);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 18, 4, 10));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        add(logo);
        JLabel sub = UIUtils.muted("Process Scheduler Simulator");
        sub.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 10));
        sub.setAlignmentX(LEFT_ALIGNMENT);
        add(sub);

        for (PageId id : PageId.values()) {
            NavButton b = new NavButton(id.getTitle());
            b.addActionListener(e -> onSelect.accept(id));
            buttons.put(id, b);
            add(b);
        }
        add(Box.createVerticalGlue());
        JLabel version = UIUtils.muted(Constants.APP_NAME);
        version.setFont(UIUtils.font(Font.PLAIN, 10));
        version.setBorder(BorderFactory.createEmptyBorder(8, 18, 14, 8));
        version.setAlignmentX(LEFT_ALIGNMENT);
        add(version);
    }

    public void select(PageId id) {
        for (Map.Entry<PageId, NavButton> e : buttons.entrySet()) {
            e.getValue().markSelected(e.getKey() == id);
        }
    }

    private static class NavButton extends JButton {
        private static final long serialVersionUID = 1L;

        private boolean selected;

        NavButton(String text) {
            super("   " + text);
            setHorizontalAlignment(LEFT);
            setFont(UIUtils.font(Font.PLAIN, 13));
            setForeground(UIUtils.TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(LEFT_ALIGNMENT);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        void markSelected(boolean selected) {
            this.selected = selected;
            setFont(UIUtils.font(selected ? Font.BOLD : Font.PLAIN, 13));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(UIUtils.ACCENT_LIGHT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UIUtils.ACCENT);
                g2.fillRect(0, 0, 4, getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
