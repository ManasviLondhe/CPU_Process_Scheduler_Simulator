package ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import util.UIUtils;

/** Simple custom-painted bar chart (no chart library). */
public class BarChartPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color[] COLORS = {
        new Color(0x0E7C86), new Color(0x2B6CB0), new Color(0x4FD1C5), new Color(0x63B3ED),
        new Color(0x319795), new Color(0x4299E1), new Color(0x2C7A7B), new Color(0x90CDF4)
    };

    private String title = "";
    private List<String> labels = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private String suffix = "";
    private boolean integers;

    public BarChartPanel() {
        setBackground(UIUtils.WHITE);
        setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        setPreferredSize(new Dimension(320, 230));
    }

    public void setData(String title, List<String> labels, List<Double> values, String suffix, boolean integers) {
        this.title = title;
        this.labels = new ArrayList<>(labels);
        this.values = new ArrayList<>(values);
        this.suffix = suffix;
        this.integers = integers;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(UIUtils.font(Font.BOLD, 12));
        g2.setColor(UIUtils.TEXT);
        g2.drawString(title, 12, 20);
        if (values.isEmpty()) {
            g2.setFont(UIUtils.font(Font.PLAIN, 12));
            g2.setColor(UIUtils.MUTED);
            g2.drawString("Run a comparison to see this chart.", 12, 50);
            g2.dispose();
            return;
        }
        int left = 14;
        int right = 14;
        int top = 44;
        int bottom = 34;
        int plotW = getWidth() - left - right;
        int plotH = getHeight() - top - bottom;
        double max = 0;
        for (double v : values) {
            max = Math.max(max, v);
        }
        if (max <= 0) {
            max = 1;
        }
        int n = values.size();
        int slot = plotW / n;
        int barW = Math.max(8, Math.min(60, slot - 12));
        g2.setColor(UIUtils.BORDER);
        g2.drawLine(left, top + plotH, left + plotW, top + plotH);
        for (int i = 0; i < n; i++) {
            int barH = (int) Math.round(plotH * values.get(i) / max);
            int x = left + i * slot + (slot - barW) / 2;
            int y = top + plotH - barH;
            g2.setColor(COLORS[i % COLORS.length]);
            g2.fillRoundRect(x, y, barW, Math.max(barH, 1), 6, 6);
            g2.setFont(UIUtils.font(Font.BOLD, 11));
            g2.setColor(UIUtils.TEXT);
            String text = integers ? String.valueOf(Math.round(values.get(i)))
                                   : String.format(Locale.US, "%.2f", values.get(i));
            text += suffix;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + (barW - fm.stringWidth(text)) / 2, y - 4);
            g2.setFont(UIUtils.font(Font.PLAIN, 10));
            g2.setColor(UIUtils.MUTED);
            fm = g2.getFontMetrics();
            String label = fit(labels.get(i), fm, slot - 4);
            g2.drawString(label, left + i * slot + (slot - fm.stringWidth(label)) / 2, top + plotH + 16);
        }
        g2.dispose();
    }

    private static String fit(String s, FontMetrics fm, int width) {
        if (fm.stringWidth(s) <= width) {
            return s;
        }
        String out = s;
        while (out.length() > 2 && fm.stringWidth(out + "..") > width) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "..";
    }
}
