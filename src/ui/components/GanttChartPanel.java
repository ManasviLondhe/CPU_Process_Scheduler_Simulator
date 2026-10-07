package ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.ScheduleSegment;
import model.SegmentType;
import util.UIUtils;

/** Draws the Gantt chart with custom Swing painting. Put it inside a JScrollPane. */
public class GanttChartPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int LEFT = 16;
    private static final int TOP = 22;
    private static final int BAR_HEIGHT = 44;
    private static final int AXIS_HEIGHT = 28;

    private transient List<ScheduleSegment> segments = new ArrayList<>();
    private int currentTime;
    private boolean showCursor;

    public GanttChartPanel() {
        setBackground(UIUtils.WHITE);
        setPreferredSize(new Dimension(400, TOP + BAR_HEIGHT + AXIS_HEIGHT + 22));
    }

    /**
     * @param segs        Gantt blocks in chronological order
     * @param time        current simulation time (cursor position)
     * @param liveCursor  draw the "now" marker and keep the newest part visible
     */
    public void setData(List<ScheduleSegment> segs, int time, boolean liveCursor) {
        this.segments = new ArrayList<>(segs);
        this.currentTime = time;
        this.showCursor = liveCursor;
        int total = totalTime();
        int width = LEFT * 2 + total * unit() + 20;
        setPreferredSize(new Dimension(width, TOP + BAR_HEIGHT + AXIS_HEIGHT + 22));
        revalidate();
        repaint();
        if (liveCursor) {
            SwingUtilities.invokeLater(() ->
                    scrollRectToVisible(new Rectangle(Math.max(0, width - 40), 0, 40, getHeight())));
        }
    }

    private int totalTime() {
        int last = segments.isEmpty() ? 0 : segments.get(segments.size() - 1).getEnd();
        return Math.max(last, currentTime);
    }

    private int unit() {
        int total = totalTime();
        if (total <= 30) {
            return 36;
        } else if (total <= 80) {
            return 22;
        } else if (total <= 200) {
            return 11;
        } else if (total <= 600) {
            return 5;
        }
        return 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (segments.isEmpty()) {
            g2.setColor(UIUtils.MUTED);
            g2.setFont(UIUtils.font(Font.PLAIN, 12));
            g2.drawString("The Gantt chart appears here as the simulation runs.", LEFT, TOP + 28);
            g2.dispose();
            return;
        }
        int unit = unit();
        g2.setFont(UIUtils.font(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        for (ScheduleSegment s : segments) {
            int x = LEFT + s.getStart() * unit;
            int w = Math.max(1, s.getLength() * unit);
            g2.setColor(fillFor(s));
            g2.fillRect(x, TOP, w, BAR_HEIGHT);
            g2.setColor(UIUtils.BORDER.darker());
            g2.drawRect(x, TOP, w, BAR_HEIGHT);
            String label = s.getLabel();
            if (fm.stringWidth(label) + 6 <= w) {
                g2.setColor(UIUtils.TEXT);
                g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, TOP + BAR_HEIGHT / 2 + fm.getAscent() / 2 - 2);
            }
        }
        drawAxis(g2, unit);
        if (showCursor) {
            int x = LEFT + currentTime * unit;
            g2.setColor(UIUtils.ACCENT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, TOP - 12, x, TOP + BAR_HEIGHT + 6);
            g2.setFont(UIUtils.font(Font.BOLD, 10));
            g2.drawString("now", x - 9, TOP - 14);
        }
        g2.dispose();
    }

    private void drawAxis(Graphics2D g2, int unit) {
        g2.setFont(UIUtils.font(Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(UIUtils.MUTED);
        int lastLabelEnd = -100;
        List<Integer> marks = new ArrayList<>();
        for (ScheduleSegment s : segments) {
            marks.add(s.getStart());
        }
        marks.add(segments.get(segments.size() - 1).getEnd());
        for (int t : marks) {
            int x = LEFT + t * unit;
            g2.drawLine(x, TOP + BAR_HEIGHT, x, TOP + BAR_HEIGHT + 5);
            String text = String.valueOf(t);
            int tx = x - fm.stringWidth(text) / 2;
            if (tx > lastLabelEnd + 4) {
                g2.drawString(text, tx, TOP + BAR_HEIGHT + 19);
                lastLabelEnd = tx + fm.stringWidth(text);
            }
        }
    }

    private Color fillFor(ScheduleSegment s) {
        if (s.getType() == SegmentType.IDLE) {
            return new Color(0xE5E7EB);
        } else if (s.getType() == SegmentType.CONTEXT_SWITCH) {
            return new Color(0xFBD38D);
        }
        return UIUtils.colorForPid(s.getPid());
    }
}
