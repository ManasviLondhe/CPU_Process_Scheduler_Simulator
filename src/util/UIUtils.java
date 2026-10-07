package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

import model.ProcessState;

/** Light "system monitor" theme: colours, fonts and small Swing helpers. */
public final class UIUtils {
    public static final Color WHITE = Color.WHITE;
    public static final Color PANEL_BG = new Color(0xF5F7FA);
    public static final Color BORDER = new Color(0xD9DEE5);
    public static final Color ACCENT = new Color(0x0E7C86);
    public static final Color ACCENT_DARK = new Color(0x0A5C64);
    public static final Color ACCENT_LIGHT = new Color(0xE0F2F3);
    public static final Color BLUE = new Color(0x2B6CB0);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color SUCCESS = new Color(0x2F855A);
    public static final Color WARNING = new Color(0xB7791F);
    public static final Color DANGER = new Color(0xC53030);

    private static final Color[] PALETTE = {
        new Color(0x90CDF4), new Color(0x9AE6B4), new Color(0xFBB6CE), new Color(0xFAF089),
        new Color(0xB794F4), new Color(0x81E6D9), new Color(0xFEB2B2), new Color(0xF6AD55),
        new Color(0xA3BFFA), new Color(0xC6F6D5)
    };

    private UIUtils() {
    }

    public static void installTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look and feel is fine as a fallback
        }
        UIManager.put("Panel.background", WHITE);
        UIManager.put("OptionPane.background", WHITE);
        UIManager.put("TextField.background", WHITE);
        UIManager.put("Table.gridColor", new Color(0xEDF0F4));
        UIManager.put("ScrollPane.background", WHITE);
        UIManager.put("Viewport.background", WHITE);
        UIManager.put("TabbedPane.background", WHITE);
        UIManager.put("TabbedPane.selected", WHITE);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("Spinner.background", WHITE);
        UIManager.put("CheckBox.background", WHITE);
        UIManager.put("RadioButton.background", WHITE);
        UIManager.put("SplitPane.background", WHITE);
    }

    public static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    public static Font mono(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }

    public static Color colorForPid(String pid) {
        return PALETTE[Math.floorMod(pid.hashCode(), PALETTE.length)];
    }

    public static Color stateColor(ProcessState state) {
        switch (state) {
            case RUNNING: return SUCCESS;
            case READY: return BLUE;
            case COMPLETED: return MUTED;
            case WAITING: return WARNING;
            default: return new Color(0x9CA3AF);
        }
    }

    /** White card with a thin border and inner padding. */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(MUTED);
        return l;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setFont(font(Font.PLAIN, 12));
        t.setForeground(TEXT);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(ACCENT_LIGHT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(font(Font.BOLD, 12));
        h.setBackground(PANEL_BG);
        h.setForeground(TEXT);
        h.setReorderingAllowed(false);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check your input", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Adds "label : component" as one row of a GridBagLayout form. */
    public static void addFormRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 12);
        JLabel l = new JLabel(label);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(TEXT);
        form.add(l, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 4);
        form.add(field, c);
    }

    public static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WHITE);
        return p;
    }
}
