package ui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Process;
import util.UIUtils;

/**
 * Draws the ready structure(s): HEAD -> [P2] -> [P4] -> TAIL. A circular queue gets a return arrow,
 * multilevel schedulers get one row per queue.
 */
public class ReadyQueuePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int BOX_W = 84;
    private static final int BOX_H = 44;
    private static final int GAP = 28;
    private static final int ROW_H = 96;

    private final Canvas canvas = new Canvas();
    private final JLabel structureLabel = UIUtils.muted(" ");

    public ReadyQueuePanel() {
        super(new BorderLayout());
        setBackground(UIUtils.WHITE);
        setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        header.add(UIUtils.title("Ready Queue"), BorderLayout.NORTH);
        structureLabel.setFont(UIUtils.font(Font.PLAIN, 11));
        header.add(structureLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(UIUtils.scroll(canvas), BorderLayout.CENTER);
    }

    /**
     * @param names      title of every queue
     * @param queues     content of every queue, head first
     * @param structure  description of the data structure, shown in the header
     */
    public void setData(List<String> names, List<List<Process>> queues, String structure) {
        structureLabel.setText(structure);
        canvas.names = new ArrayList<>(names);
        canvas.queues = new ArrayList<>();
        for (List<Process> q : queues) {
            canvas.queues.add(new ArrayList<>(q));
        }
        canvas.setPreferredSize(new Dimension(maxWidth(queues), 28 + Math.max(1, queues.size()) * ROW_H));
        canvas.revalidate();
        canvas.repaint();
    }

    private static int maxWidth(List<List<Process>> queues) {
        int max = 3;
        for (List<Process> q : queues) {
            max = Math.max(max, q.size());
        }
        return 40 + max * (BOX_W + GAP) + 40;
    }

    private static class Canvas extends JPanel {
        private static final long serialVersionUID = 1L;

        private List<String> names = new ArrayList<>();
        private List<List<Process>> queues = new ArrayList<>();

        Canvas() {
            setBackground(UIUtils.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int row = 0; row < queues.size(); row++) {
                drawRow(g2, row, names.get(row), queues.get(row));
            }
            g2.dispose();
        }

        private void drawRow(Graphics2D g2, int row, String name, List<Process> q) {
            int y = 10 + row * ROW_H;
            g2.setFont(UIUtils.font(Font.BOLD, 12));
            g2.setColor(UIUtils.TEXT);
            g2.drawString(name, 14, y + 12);
            int boxY = y + 36;
            if (q.isEmpty()) {
                g2.setFont(UIUtils.font(Font.ITALIC, 12));
                g2.setColor(UIUtils.MUTED);
                g2.drawString("(empty)", 20, boxY + BOX_H / 2 + 4);
                return;
            }
            for (int i = 0; i < q.size(); i++) {
                int x = 20 + i * (BOX_W + GAP);
                drawBox(g2, q.get(i), x, boxY);
                if (i == 0) {
                    tag(g2, "HEAD", x, boxY - 6);
                }
                if (i == q.size() - 1) {
                    tag(g2, "TAIL", x + (i == 0 ? BOX_W - 28 : 0), boxY - 6);
                }
                if (i < q.size() - 1) {
                    arrow(g2, x + BOX_W, boxY + BOX_H / 2, x + BOX_W + GAP, boxY + BOX_H / 2);
                }
            }
            if ((name.contains("Circular") || name.contains("Round Robin")) && q.size() > 1) {
                drawReturnArrow(g2, 20 + BOX_W / 2, 20 + (q.size() - 1) * (BOX_W + GAP) + BOX_W / 2, boxY + BOX_H);
            }
        }

        private void drawBox(Graphics2D g2, Process p, int x, int y) {
            g2.setColor(UIUtils.colorForPid(p.getPid()));
            g2.fillRoundRect(x, y, BOX_W, BOX_H, 10, 10);
            g2.setColor(UIUtils.MUTED);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, BOX_W, BOX_H, 10, 10);
            g2.setFont(UIUtils.font(Font.BOLD, 13));
            g2.setColor(UIUtils.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(p.getPid(), x + (BOX_W - fm.stringWidth(p.getPid())) / 2, y + 18);
            g2.setFont(UIUtils.font(Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            String sub = "rem " + p.getRemainingTime() + " | pr " + p.getEffectivePriority();
            g2.drawString(sub, x + (BOX_W - fm.stringWidth(sub)) / 2, y + 35);
        }

        private void tag(Graphics2D g2, String text, int x, int y) {
            g2.setFont(UIUtils.font(Font.BOLD, 10));
            g2.setColor(UIUtils.ACCENT_DARK);
            g2.drawString(text, x, y);
        }

        private void arrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.setColor(UIUtils.MUTED);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x1 + 2, y1, x2 - 2, y2);
            g2.drawLine(x2 - 2, y2, x2 - 8, y2 - 4);
            g2.drawLine(x2 - 2, y2, x2 - 8, y2 + 4);
        }

        private void drawReturnArrow(Graphics2D g2, int xFirst, int xLast, int yBottom) {
            g2.setColor(UIUtils.ACCENT);
            g2.setStroke(new BasicStroke(1.5f));
            Path2D path = new Path2D.Double();
            path.moveTo(xLast, yBottom);
            path.lineTo(xLast, yBottom + 14);
            path.lineTo(xFirst, yBottom + 14);
            path.lineTo(xFirst, yBottom + 2);
            g2.draw(path);
            g2.drawLine(xFirst, yBottom + 2, xFirst - 4, yBottom + 8);
            g2.drawLine(xFirst, yBottom + 2, xFirst + 4, yBottom + 8);
            g2.setFont(UIUtils.font(Font.PLAIN, 10));
            g2.drawString("circular: rear wraps to front", (xFirst + xLast) / 2 - 70, yBottom + 27);
        }
    }
}
